package backendlab.team4you.s3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    S3Client s3Client;

    @InjectMocks
    S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", "team4you-files");
    }

    @Test
    void uploadFile_shouldCallPutObject() {
        // Arrange
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // Act
        s3Service.uploadFile("test.txt", "Hello".getBytes(), "text/plain");

        // Assert — verify that putObject was called once
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void deleteFile_shouldCallDeleteObject() {

        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());


        s3Service.deleteFile("test.txt");

        // Assert — verify that deleteObject was called once
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }
    @Test
    void downloadFile_shouldCallGetObject() {
        // Arrange
        ResponseInputStream<GetObjectResponse> mockStream = mock(ResponseInputStream.class);
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(mockStream);

        // Act
        s3Service.downloadFile("test.txt");

        // Assert — verify that getObject was called once
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }
}