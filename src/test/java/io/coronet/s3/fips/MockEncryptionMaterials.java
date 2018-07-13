package io.coronet.s3.fips;

import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.EncryptionMaterialsProvider;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

/**
 * Mock encryption materials provider that provides a static key.
 */
public class MockEncryptionMaterials implements EncryptionMaterialsProvider {

    private final SecretKey key = new SecretKeySpec(new byte[32], "AES");

    public EncryptionMaterials getEncryptionMaterials() {
        return new EncryptionMaterials(key);
    }

    public EncryptionMaterials getEncryptionMaterials(Map<String, String> materialsDescription) {
        return new EncryptionMaterials(key);
    }

    public void refresh() {}
}
