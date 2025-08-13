package run.soeasy.starter.commons.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlFormat extends XmlMapper implements JacksonFormat {
	private static final long serialVersionUID = 1L;
	
	public static final XmlFormat DEFAULT = new XmlFormat();

	@Override
	public ObjectMapper getObjectMapper() {
		return this;
	}
}