package com.shrija.recruitment.agent;

/**
 * Documents the OfferTool capability boundary.
 *
 * <p>Actual execution is provided by the shared MCP server through RecruitmentMcpClient.
 * This class does not duplicate recruitment business logic.
 */
public final class OfferTool {
  private OfferTool() {}
  public static final String[] FUNCTIONS = {"generateOffer"};
}
