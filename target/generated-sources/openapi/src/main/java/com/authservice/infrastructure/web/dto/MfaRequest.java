package com.authservice.infrastructure.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MfaRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-12-27T19:02:17.923228-05:00[America/New_York]")
public class MfaRequest {

  private String mfaCode;

  public MfaRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public MfaRequest(String mfaCode) {
    this.mfaCode = mfaCode;
  }

  public MfaRequest mfaCode(String mfaCode) {
    this.mfaCode = mfaCode;
    return this;
  }

  /**
   * Get mfaCode
   * @return mfaCode
  */
  @NotNull 
  @Schema(name = "mfaCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("mfaCode")
  public String getMfaCode() {
    return mfaCode;
  }

  public void setMfaCode(String mfaCode) {
    this.mfaCode = mfaCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MfaRequest mfaRequest = (MfaRequest) o;
    return Objects.equals(this.mfaCode, mfaRequest.mfaCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mfaCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MfaRequest {\n");
    sb.append("    mfaCode: ").append(toIndentedString(mfaCode)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

