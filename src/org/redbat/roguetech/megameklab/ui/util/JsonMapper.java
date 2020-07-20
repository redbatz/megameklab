package org.redbat.roguetech.megameklab.ui.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class JsonMapper extends com.fasterxml.jackson.databind.json.JsonMapper {

    public JsonMapper() {
        this.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SET).withValueNulls(Nulls.SET));
        this.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS));
        this.enable(SerializationFeature.INDENT_OUTPUT);
        this.enable(JsonParser.Feature.ALLOW_COMMENTS);
        this.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
//        this.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
//        this.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        DefaultPrettyPrinter prettyPrinter = new RoguetechPrettyPrinter();
        this.setDefaultPrettyPrinter(prettyPrinter);
//        registerCustomSerializers();
    }

//    public void registerCustomSerializers() {
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addSerializer(double.class, new DoubleSerializer());
//        simpleModule.addSerializer(Double.class, new DoubleSerializer());
//        this.registerModule(simpleModule);
//    }

    private static class RoguetechPrettyPrinter extends DefaultPrettyPrinter {

        RoguetechPrettyPrinter() {
            this._arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new RoguetechPrettyPrinter();
        }

        @Override
        public void beforeObjectEntries(JsonGenerator g) throws IOException {
            this._objectIndenter.writeIndentation(g, this._nesting);
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
            g.writeRaw(_separators.getObjectFieldValueSeparator() + " ");
        }

        @Override
        public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
            if (nrOfValues == 0) {
                if (!_arrayIndenter.isInline()) {
                    --_nesting;
                }
                g.writeRaw(']');
            } else {
                super.writeEndArray(g, nrOfValues);
            }
        }
    }
}
