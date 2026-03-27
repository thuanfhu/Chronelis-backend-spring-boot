package com.devloopsx.chronelis.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsS3Configuration {
	@Value("${aws.credentials.accessKey}")
	private String AWS_ACCESS_KEY;

	@Value("${aws.credentials.secretKey}")
	private String AWS_SECRET_KEY;

	@Value("${aws.s3.region}")
	private String S3_REGION;

	@Bean
	public S3Client s3Client() {
		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(AWS_ACCESS_KEY, AWS_SECRET_KEY);

		return S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
				.region(Region.of(S3_REGION)).build();
	}
}
