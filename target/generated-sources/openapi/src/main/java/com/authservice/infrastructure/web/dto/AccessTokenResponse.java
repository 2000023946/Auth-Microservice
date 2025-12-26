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
 * AccessTokenResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-12-26T00:03:44.423679-05:00[America/New_York]")
public class AccessTokenResponse {

  private String access;

  public AccessTokenResponse access(String access) {
    this.access = access;
    return this;
  }

  /**
   * Get access
   * @return access
  */
  
  @Schema(name = "access", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("access")
  public String getAccess() {
    return access;
  }

  public void setAccess(String access) {
    this.access = access;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccessTokenResponse accessTokenResponse = (AccessTokenResponse) o;
    return Objects.equals(this.access, accessTokenResponse.access);
  }

  @Override
  public int hashCode() {
    return Objects.hash(access);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccessTokenResponse {\n");
    sb.append("    access: ").append(toIndentedString(access)).append("\n");
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

