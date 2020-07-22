package com.solace.connector.mulesoft.internal;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.resolving.OutputStaticTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;

/**
 * An {@link OutputTypeResolver} for JMS operations
 *
 * @since 1.0
 */
public class SolaceOutputResolver extends OutputStaticTypeResolver {

  @Override
  public String getCategoryName() {
    return "SolaceMetadata";
  }

  @Override
  public MetadataType getStaticMetadata() {
    return new BaseTypeBuilder(JAVA).anyType().build();
  }
}
