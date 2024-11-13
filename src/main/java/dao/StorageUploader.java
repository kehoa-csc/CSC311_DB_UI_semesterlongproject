package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class StorageUploader {
    private BlobContainerClient containerClient;

    public StorageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;AccountName=kehoacsc311storage;AccountKey=JjETGfS9kACXXK7rixYbKy07OzZ/xYptYV7oUP6q37vq4JNPoJSaFKp9SD2Ts1m5Bg1fS39zetVb+ASti4ANbQ==;EndpointSuffix=core.windows.net")
                .containerName("media-files")
                .buildClient();
    }

    public void uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath,true);
    }
    public BlobContainerClient getContainerClient(){
        return containerClient;
    }
}
