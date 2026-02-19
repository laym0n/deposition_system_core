package com.deponic.domain.adapter;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlFileBuilder {

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    private XmlFileBuilder() {
    }

    public static Resource createXmlResource(Object object, String sourceFilename) {
        var metadataFilename = buildMetadataFilename(sourceFilename);
        var xmlPayload = buildXmlBytes(object);

        return createXmlResourceInternal(xmlPayload, metadataFilename);
    }

    public static Resource createXmlResource(byte[] xmlPayload, String sourceFilename) {
        var metadataFilename = buildMetadataFilename(sourceFilename);

        return createXmlResourceInternal(xmlPayload, metadataFilename);
    }

    public static byte[] buildXmlBytes(Object object) {
        return build(object).getBytes(StandardCharsets.UTF_8);
    }

    private static Resource createXmlResourceInternal(byte[] xmlPayload, String metadataFilename) {

        return new ByteArrayResource(xmlPayload) {

            @Override
            public String getFilename() {
                return metadataFilename;
            }

        };
    }

    private static String build(Object object) {
        try {
            return XML_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to convert file metadata to XML", exception);
        }
    }

    private static String buildMetadataFilename(String sourceFilename) {
        if (sourceFilename == null || sourceFilename.isBlank()) {
            return "premis-metadata.xml";
        }

        return sourceFilename + ".premis.xml";
    }
}
