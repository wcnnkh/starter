package run.soeasy.starter.payment.apple;

import java.util.List;

import lombok.Data;

/**
 * <a href=
 * "https://developer.apple.com/documentation/appstorereceipts/responsebody/receipt">文档</a>
 * 
 * @author soeasy.run
 *
 */
@Data
public class Receipt {

	/**
	 * 请参阅。app_item_id
	 */
	private Long adamId;
	/**
	 * 由App Store Connect生成，并由App Store用于唯一标识购买的应用。仅在生产中为应用程序分配此标识符。将此值视为64位长整数。
	 * 
	 */
	private Long appItemId;

	/**
	 * 应用程序的版本号。该应用程序的版本号对应于（在iOS中）或（在macOS中）中的值。在生产中，此值为基于的设备上应用程序的当前版本。在沙盒中，该值始终为。CFBundleVersionCFBundleShortVersionStringInfo.plistreceipt_creation_date_ms"1.0"
	 */
	private String applicationVersion;

	/**
	 * 收据所属应用的捆绑标识符。您在App Store
	 * Connect上提供此字符串。这相当于价值中的应用程序的文件。CFBundleIdentifierInfo.plist
	 */
	private String bundleId;

	/**
	 * 应用下载交易的唯一标识符。
	 */
	private Integer downloadId;

	/**
	 * 通过批量购买计划购买的应用程序的收据过期时间.
	 */
	private String expirationDate;
	private Long expirationDateMs;
	private String expirationDatePst;

	/**
	 * 包含所有应用内购买交易的应用内购买收据字段的数组。 以根据purchase_date升序排列
	 */
	private List<InApp> inApps;

	/**
	 * 用户最初购买的应用程序的版本。该值不变，并且与原始购买文件中的（在iOS中）或String（在macOS中）的值相对应。在沙盒环境中，该值始终为。CFBundleVersionCFBundleShortVersionInfo.plist"1.0"
	 * 
	 */
	private String originalApplicationVersion;

	/**
	 * 原始应用购买时间
	 */
	private String originalPurchaseDate;
	private Long originalPurchaseDateMs;
	private String originalPurchaseDatePst;

	/**
	 * 用户订购可用于预订的应用的时间
	 */
	private String preorderDate;
	private Long preorderDateMs;
	private String preorderDatePst;

	/**
	 * App Store生成收据的时间
	 * 
	 */
	private String receiptCreationDate;
	private Long receiptCreationDateMs;
	private String receiptCreationDatePst;

	/**
	 * 生成的收据类型。该值对应于购买应用程序或VPP的环境。可能的值： Production, ProductionVPP,
	 * ProductionSandbox, ProductionVPPSandbox
	 * 
	 */
	private String receiptType;

	/**
	 * 对端点的请求并生成响应的时间
	 */
	private String requestDate;
	private Long requestDateMs;
	private String requestDatePst;

	/**
	 * 标识应用程序修订版的任意数字。在沙盒中，此键的值为“0”。
	 */
	private Integer versionExternalIdentifier;

	/**
	 * ios7之前存在此字段，之后使用in_apps的第一个
	 * 
	 */
	private String getProductId;
}
