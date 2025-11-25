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

/**
 * Spring 资源适配器，用于适配 Spring {@link Resource} 与自定义框架资源接口的桥接。
 * <p>
 * 该类包装了 Spring 的 {@link Resource} 对象，实现了自定义的 {@link run.soeasy.framework.io.Resource} 接口，
 * 使得 Spring 资源可以在自定义框架中统一使用，同时保留了 Spring 资源的原有功能。
 * <p>
 * 主要特性：
 * <ul>
 *     <li>包装 Spring 资源对象，提供统一的资源访问接口</li>
 *     <li>支持可写资源判断及输出流获取（仅当底层资源为 {@link WritableResource} 时）</li>
 *     <li>提供静态方法快速将 Spring 资源数组转换为适配器数组</li>
 * </ul>
 *
 * @author soeasy.run
 */
@Getter
@RequiredArgsConstructor
public class ResourceAdapter implements run.soeasy.framework.io.Resource, Resource {

    /**
     * 被包装的 Spring 资源对象，不能为空。
     */
    @NonNull
    private final Resource resource;

    /**
     * 获取资源的输入流。
     *
     * @return 资源的输入流
     * @throws IOException 如果资源无法读取或输入流获取失败
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    /**
     * 获取资源的输出流（仅支持可写资源）。
     * <p>
     * 如果底层资源实现了 {@link WritableResource} 接口，则返回其输出流；
     * 否则抛出 {@link UnsupportedOperationException} 异常。
     *
     * @return 资源的输出流
     * @throws IOException 如果资源无法写入或输出流获取失败
     * @throws UnsupportedOperationException 如果资源不支持写入操作
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (resource instanceof WritableResource) {
            return ((WritableResource) resource).getOutputStream();
        }
        throw new UnsupportedOperationException("write");
    }

    /**
     * 判断资源是否可写。
     * <p>
     * 仅当底层资源为 {@link WritableResource} 类型时返回 {@code true}。
     *
     * @return 资源是否可写
     */
    @Override
    public boolean isWritable() {
        return resource instanceof WritableResource;
    }

    /**
     * 获取资源的最后修改时间戳。
     *
     * @return 资源最后修改时间戳（毫秒级）
     * @throws IOException 如果资源信息获取失败
     */
    @Override
    public long lastModified() throws IOException {
        return resource.lastModified();
    }

    /**
     * 判断资源是否可读。
     *
     * @return 资源是否可读
     */
    @Override
    public boolean isReadable() {
        return resource.isReadable();
    }

    /**
     * 获取资源的描述信息（通常包含资源路径等）。
     *
     * @return 资源的描述字符串
     */
    @Override
    public String getDescription() {
        return resource.getDescription();
    }

    /**
     * 获取资源的 URL 地址。
     *
     * @return 资源的 URL
     * @throws IOException 如果资源无法解析为 URL
     */
    @Override
    public URL getURL() throws IOException {
        return resource.getURL();
    }

    /**
     * 获取资源的 URI 地址。
     *
     * @return 资源的 URI
     * @throws IOException 如果资源无法解析为 URI
     */
    @Override
    public URI getURI() throws IOException {
        return resource.getURI();
    }

    /**
     * 获取资源对应的文件对象（仅适用于文件系统中的资源）。
     *
     * @return 资源对应的文件
     * @throws IOException 如果资源无法转换为文件或不存在
     */
    @Override
    public File getFile() throws IOException {
        return resource.getFile();
    }

    /**
     * 创建相对于当前资源的新资源。
     *
     * @param relativePath 相对路径
     * @return 相对资源对应的 Spring {@link Resource} 对象
     * @throws IOException 如果相对资源创建失败
     */
    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return resource.createRelative(relativePath);
    }

    /**
     * 获取资源的文件名（如果有）。
     *
     * @return 资源的文件名，无文件名时返回 {@code null}
     */
    @Override
    public String getFilename() {
        return resource.getFilename();
    }

    /**
     * 获取资源的可读字节通道。
     *
     * @return 资源的可读字节通道
     * @throws IOException 如果通道创建失败
     */
    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        return resource.readableChannel();
    }

    /**
     * 判断资源是否处于打开状态。
     *
     * @return 资源是否打开
     */
    @Override
    public boolean isOpen() {
        return resource.isOpen();
    }

    /**
     * 获取资源的内容长度。
     *
     * @return 资源的内容长度（字节数）
     * @throws IOException 如果长度获取失败
     */
    @Override
    public long contentLength() throws IOException {
        return resource.contentLength();
    }

    /**
     * 判断资源是否存在。
     *
     * @return 资源是否存在
     */
    @Override
    public boolean exists() {
        return resource.exists();
    }
    
    @Override
    public String toString() {
    	return resource.toString();
    }

    /**
     * 将 Spring 资源数组转换为资源适配器数组。
     * <p>
     * 该方法会为每个非空的 Spring {@link Resource} 创建对应的 {@link ResourceAdapter}，
     * 若输入数组中存在 {@code null} 元素，则对应的适配器元素也为 {@code null}。
     *
     * @param resources Spring 资源数组
     * @return 资源适配器数组，长度与输入数组一致
     */
    public static ResourceAdapter[] forResources(Resource... resources) {
    	if(resources == null) {
    		return null;
    	}
        ResourceAdapter[] springResources = new ResourceAdapter[resources.length];
        for (int i = 0; i < resources.length; i++) {
            springResources[i] = resources[i] == null ? null : new ResourceAdapter(resources[i]);
        }
        return springResources;
    }
}