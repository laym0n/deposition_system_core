package com.deposition.domain.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import com.deposition.domain.dto.schema.premis.v3.ObjectFactory;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class XmlUtils {

    private static final String PREMIS_SCHEMA_CLASSPATH_LOCATION = "schema/premis-v3-0.xsd";
    private static final XmlMapper XML_MAPPER = new XmlMapper();
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final JAXBContext PREMIS_JAXB_CONTEXT = initPremisJaxbContext();
    private static final Schema PREMIS_SCHEMA = initPremisSchema();

    private XmlUtils() {
    }

    public static Resource createXmlResource(Object object, String sourceFilename) {
        var metadataFilename = buildMetadataFilename(sourceFilename);
        var xmlPayload = buildXmlBytes(object);

        return createXmlResourceInternal(xmlPayload, metadataFilename);
    }

    public static PremisComplexType parsePremis(Resource premisXml) {
        try (var inputStream = premisXml.getInputStream()) {
            var xml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            var unmarshaller = createPremisUnmarshaller();
            var result = unmarshaller.unmarshal(new StringReader(xml));

            if (result instanceof JAXBElement<?> jaxbElement
                    && jaxbElement.getValue() instanceof PremisComplexType premis) {
                return premis;
            }

            if (result instanceof PremisComplexType premis) {
                return premis;
            }

            throw new IllegalStateException(
                    "Failed to unmarshal PREMIS metadata: unexpected root type=" + result.getClass().getName());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to unmarshal PREMIS metadata", exception);
        }
    }

    private static byte[] buildXmlBytes(Object object) {
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
        if (object instanceof PremisComplexType premis) {
            return marshalPremis(premis);
        }

        try {
            return XML_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to convert file metadata to XML", exception);
        }
    }

    private static String marshalPremis(PremisComplexType premis) {
        try {
            var marshaller = createPremisMarshaller();
            var writer = new StringWriter();

            JAXBElement<PremisComplexType> rootElement = OBJECT_FACTORY.createPremis(premis);
            marshaller.marshal(rootElement, writer);

            return writer.toString();
        } catch (JAXBException exception) {
            throw new IllegalStateException("Failed to marshal PREMIS metadata using JAXB", exception);
        }
    }

    private static Marshaller createPremisMarshaller() throws JAXBException {
        var marshaller = PREMIS_JAXB_CONTEXT.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setSchema(PREMIS_SCHEMA);
        return marshaller;
    }

    private static Unmarshaller createPremisUnmarshaller() throws JAXBException {
        var unmarshaller = PREMIS_JAXB_CONTEXT.createUnmarshaller();
        unmarshaller.setSchema(PREMIS_SCHEMA);
        return unmarshaller;
    }

    private static JAXBContext initPremisJaxbContext() {
        try {
            return JAXBContext.newInstance(PremisComplexType.class);
        } catch (JAXBException exception) {
            throw new IllegalStateException("Failed to initialize JAXB context for PREMIS", exception);
        }
    }

    private static Schema initPremisSchema() {
        try {
            var schemaUrl = XmlUtils.class.getClassLoader().getResource(PREMIS_SCHEMA_CLASSPATH_LOCATION);
            if (schemaUrl == null) {
                throw new IllegalStateException(
                        "PREMIS schema not found on classpath: " + PREMIS_SCHEMA_CLASSPATH_LOCATION);
            }

            var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return schemaFactory.newSchema(schemaUrl);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialize PREMIS XML Schema", exception);
        }
    }

    private static String buildMetadataFilename(String sourceFilename) {
        if (sourceFilename == null || sourceFilename.isBlank()) {
            return "premis-metadata.xml";
        }

        return sourceFilename + ".premis.xml";
    }
}
