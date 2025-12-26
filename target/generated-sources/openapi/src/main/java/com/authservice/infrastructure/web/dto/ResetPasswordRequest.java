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
 * ResetPasswordRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-12-26T00:03:44.423679-05:00[America/New_York]")
public class ResetPasswordRequest {

  private String resetToken;

  private String password;

  private String passwordRepeat;

  public ResetPasswordRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ResetPasswordRequest(String resetToken, String password, String passwordRepeat) {
    this.resetToken = resetToken;
    this.password = password;
    this.passwordRepeat = passwordRepeat;
  }

  public ResetPasswordRequest resetToken(String resetToken) {
    this.resetToken = resetToken;
    return this;
  }

  /**
   * Get resetToken
   * @return resetToken
  */
  @NotNull 
  @Schema(name = "resetToken", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("resetToken")
  public String getResetToken() {
    return resetToken;
  }

  public void setResetToken(String resetToken) {
    this.resetToken = resetToken;
  }

  public ResetPasswordRequest password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Get password
   * @return password
  */
  @NotNull 
  @Schema(name = "password", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public ResetPasswordRequest passwordRepeat(String passwordRepeat) {
    this.passwordRepeat = passwordRepeat;
    return this;
  }

  /**
   * Get passwordRepeat
   * @return passwordRepeat
  */
  @NotNull 
  @Schema(name = "passwordRepeat", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("passwordRepeat")
  public String getPasswordRepeat() {
    return passwordRepeat;
  }

  public void setPasswordRepeat(String passwordRepeat) {
    this.passwordRepeat = passwordRepeat;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResetPasswordRequest resetPasswordRequest = (ResetPasswordRequest) o;
    return Objects.equals(this.resetToken, resetPasswordRequest.resetToken) &&
        Objects.equals(this.password, resetPasswordRequest.password) &&
        Objects.equals(this.passwordRepeat, resetPasswordRequest.passwordRepeat);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resetToken, password, passwordRepeat);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResetPasswordRequest {\n");
    sb.append("    resetToken: ").append(toIndentedString(resetToken)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    passwordRepeat: ").append(toIndentedString(passwordRepeat)).append("\n");
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

