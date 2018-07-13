# S3 FIPS Encryption Demo

The [AWS SDK for Java](https://github.com/aws/aws-sdk-java/) includes support for client-side encryption
of data before uploading it to S3. This little sample demonstrates how to configure it to use a
[FIPS](https://en.wikipedia.org/wiki/FIPS_140-2)-certified implementation: specifically
https://www.bouncycastle.org/fips-java/.

TL;DR if you don't want to actually look at the code:

```
    BouncyCastleFipsProvider provider = new BouncyCastleFipsProvider();
    SecureRandom srand = SecureRandom.getInstance("DEFAULT", provider);

    AmazonS3 s3 = AmazonS3EncryptionClientBuilder.standard()
            // {normal credential/key material bits go here}
            .withCryptoConfiguration(new CryptoConfiguration()
                    .withCryptoProvider(provider)
                    .withAlwaysUseCryptoProvider(true)
                    .withSecureRandom(srand)
                    .withCryptoMode(CryptoMode.StrictAuthenticatedEncryption))
            .build();
```

Yes, you need to explicitly tell it to **always** use the FIPS-certified crypto provider.
Previous versions of the library always used the non-FIPS-certifed BouncyCastle
implementation in `StrictAuthenticatedEncryption` mode, ignoring the user-specified
crypto provider, and the team at AWS feels strongly about backwards compatibility.
