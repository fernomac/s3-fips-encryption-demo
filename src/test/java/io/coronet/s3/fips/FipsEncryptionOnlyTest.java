package io.coronet.s3.fips;

import com.adobe.testing.s3mock.junit4.S3MockRule;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.CryptoMode;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * An example of using the {@code AmazonS3EncryptionClient} in encryption-only
 * mode with the FIPS-certified BouncyCastle implementation.
 */
public class FipsEncryptionOnlyTest {

    private static final String BUCKET_NAME = "bogus";
    private static final String OBJECT_KEY = "also-bogus";
    private static final String PLAINTEXT = "T0p S3cr3t!!1";

    @Rule
    public final S3MockRule S3_MOCK = S3MockRule.builder()
            .withInitialBuckets(BUCKET_NAME)
            .silent()
            .build();

    @Test
    public void testIt() throws Exception {
        // Tell BouncyCastle to only let us use FIPS-approved algorithms.
        System.setProperty("org.bouncycastle.fips.approved_only", "true");

        BouncyCastleFipsProvider provider = new BouncyCastleFipsProvider();
        SecureRandom srand = SecureRandom.getInstance("DEFAULT", provider);

        AmazonS3 s3 = AmazonS3EncryptionClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("foo", "bar")))
                .withEncryptionMaterials(new MockEncryptionMaterials())
                .withClientConfiguration(S3_MOCK.configureClientToIgnoreInvalidSslCertificates(new ClientConfiguration()))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://localhost:" + S3_MOCK.getPort(), "us-localhost-1"))
                .withPathStyleAccessEnabled(true)

        // The important bits for this demonstration:
                .withCryptoConfiguration(new CryptoConfiguration()
                        .withCryptoProvider(provider)        // Use the FIPS-certified BouncyCastle provider.
                        .withSecureRandom(srand)             // Use a SecureRandom instance from BC-FIPS as well (for CEK and IV generation).
                        .withCryptoMode(CryptoMode.EncryptionOnly))

                .build();

        // Store (and presumably encrypt) the object.
        PutObjectResult result = s3.putObject(BUCKET_NAME, OBJECT_KEY, PLAINTEXT);

        // Read it back and make sure it can be decrypted.
        try (S3Object obj = s3.getObject(BUCKET_NAME, OBJECT_KEY)) {
            String actual = new String(obj.getObjectContent().readAllBytes(), StandardCharsets.UTF_8);
            Assert.assertEquals(PLAINTEXT, actual);
        }
    }
}
