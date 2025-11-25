package run.soeasy.starter.common.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceAdapter implements run.soeasy.framework.io.Resource, Resource {
	@NonNull
	private final Resource resource;

	@Override
	public InputStream getInputStream() throws IOException {
		return resource.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (resource instanceof WritableResource) {
			return ((WritableResource) resource).getOutputStream();
		}
		throw new UnsupportedOperationException("write");
	}

	@Override
	public boolean isWritable() {
		return resource instanceof WritableResource;
	}

	@Override
	public long lastModified() throws IOException {
		return resource.lastModified();
	}

	@Override
	public boolean isReadable() {
		return resource.isReadable();
	}

	@Override
	public String getDescription() {
		return resource.getDescription();
	}

	@Override
	public URL getURL() throws IOException {
		return resource.getURL();
	}

	@Override
	public URI getURI() throws IOException {
		return resource.getURI();
	}

	@Override
	public File getFile() throws IOException {
		return resource.getFile();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		return resource.createRelative(relativePath);
	}

	@Override
	public String getFilename() {
		return resource.getFilename();
	}

	@Override
	public ReadableByteChannel readableChannel() throws IOException {
		return resource.readableChannel();
	}

	@Override
	public boolean isOpen() {
		return resource.isOpen();
	}

	@Override
	public long contentLength() throws IOException {
		return resource.contentLength();
	}

	@Override
	public boolean exists() {
		return resource.exists();
	}

	public static ResourceAdapter[] forResources(@NonNull Resource... resources) {
		ResourceAdapter[] springResources = new ResourceAdapter[resources.length];
		for (int i = 0; i < resources.length; i++) {
			springResources[i] = resources[i] == null ? null : new ResourceAdapter(resources[i]);
		}
		return springResources;
	}
}
