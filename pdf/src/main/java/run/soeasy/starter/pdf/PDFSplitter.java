package run.soeasy.starter.pdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import lombok.Getter;
import lombok.NonNull;
import run.soeasy.framework.core.RandomUtils;

/**
 * PDF 按页拆分器（实现 {@link PDFReadListener} 接口，支持将 PDF 文档全页/指定页码拆分为独立临时文件）
 * <p>
 * 核心功能：通过监听 PDF 逐页解析事件，为目标页面（全页/指定页）创建独立 PDF 临时文件，自动收集拆分结果，
 * 无需手动管理文档生命周期和文件创建逻辑，提供实例化使用和静态快捷两种调用方式，简化 PDF 拆分流程。
 * </p>
 * <p>
 * 特性说明： 1. 拆分文件：临时文件命名规则为「UUID + 原文档页码索引 + .pdf」，确保全局文件名唯一； 2.
 * 结果顺序：拆分后文件列表顺序与解析顺序一致（全页解析=原文档顺序，指定页码解析=传入页码顺序）； 3. 属性保留：基于 PDFBox
 * 实现，完整保留原页面旋转角度、内容、字体、注释等属性； 4. 资源安全：自动关闭拆分过程中创建的临时文档，避免文件句柄泄露； 5.
 * 灵活调用：支持实例化后配合 {@link PDFUtils} 自定义解析流程，或直接调用静态 {@link #load} 方法一键拆分。
 * </p>
 * <p>
 * 注意事项： 1. 拆分后的文件为临时文件，使用完成后需手动删除或迁移至目标目录，避免占用磁盘空间； 2. 静态 {@link #load}
 * 方法内部会创建新的 {@link PDFSplitter} 实例，与外部实例的 {@link #outputFiles} 相互独立； 3.
 * 指定页码解析时，页码需为 0-based 有效索引（范围：0 ~ 原文档总页数-1），无效页码会抛出异常。
 * </p>
 */
@Getter // Lombok 注解：生成 outputFiles 字段的 getter 方法，用于获取拆分后的文件列表
public class PDFSplitter implements PDFReadListener {

	/**
	 * 拆分后的 PDF 临时文件列表
	 * <p>
	 * 列表有效性：仅在 PDF 解析完成后有效，解析过程中动态添加； 列表顺序规则： - 全页解析：与原 PDF 页面顺序一致（列表索引 0 对应原文档第 1
	 * 页，列表索引 N 对应原文档第 N+1 页）； - 指定页码解析：与传入的 {@code pageIndexs}
	 * 顺序一致（非原文档顺序，重复页码会重复添加）。
	 */
	private final List<File> outputFiles = new ArrayList<>();

	/**
	 * 逐页解析成功后的核心拆分逻辑（实现 {@link PDFReadListener} 接口的抽象方法）
	 * <p>
	 * 触发时机：每一页目标页面（全页/指定页）解析完成后同步回调，逻辑流程： 1. 创建空的临时 PDF 文档（仅存储当前解析页）； 2.
	 * 将当前页添加到临时文档（完整保留原页面属性）； 3. 生成唯一命名的临时文件并持久化； 4. 将临时文件引用添加到结果列表（保持解析顺序）； 5.
	 * 强制关闭临时文档，释放资源（无论保存成功与否）。
	 * </p>
	 *
	 * @param document  全局 PDF 文档对象（非 null，由 {@link PDFUtils} 管理生命周期，请勿手动关闭）
	 * @param page      当前解析完成的页面对象（非 null，包含原页面完整属性）
	 * @param pageIndex 页面索引（从 0 开始，与原文档页面顺序完全一致，用于临时文件命名）
	 * @return boolean - 固定返回 true，确保所有目标页面（全页/指定页）均被拆分
	 * @throws IOException 拆分过程中发生的 IO 异常（如临时文件创建失败、文档保存失败、磁盘空间不足等）
	 */
	@Override
	public boolean onPageParsed(@NonNull PDDocument document, @NonNull PDPage page, int pageIndex) throws IOException {
		// 创建临时文档（仅用于存储当前解析页，独立于源文档）
		PDDocument newDocument = new PDDocument();
		try {
			newDocument.addPage(page);
			// 生成唯一临时文件：UUID 前缀避免冲突，原文档页码索引作为标识便于追溯
			File tempFile = File.createTempFile(RandomUtils.uuid(), pageIndex + ".pdf");
			newDocument.save(tempFile);
			// 按解析顺序添加到结果列表
			outputFiles.add(tempFile);
		} finally {
			// 强制关闭临时文档，避免资源泄露（finally 块确保必执行）
			newDocument.close();
		}
		// 返回 true 表示继续解析下一个目标页面
		return true;
	}
}