package com.mapbox.mapboxsdk.attribution;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;

import java.util.LinkedHashSet;
import java.util.Set;

public class AttributionParser {

  private String attributionDataString;
  private boolean withImproveMap;
  private boolean withCopyrightSign;
  private boolean withTelemetryAttribution;
  private boolean withMapboxAttribution;

  AttributionParser(String attributionDataString, boolean withImproveMap, boolean withCopyrightSign,
                    boolean withTelemetryAttribution, boolean withMapboxAttribution) {
    this.attributionDataString = attributionDataString;
    this.withImproveMap = withImproveMap;
    this.withCopyrightSign = withCopyrightSign;
    this.withTelemetryAttribution = withTelemetryAttribution;
    this.withMapboxAttribution = withMapboxAttribution;
  }

  public Set<Attribution> parse() {
    Set<Attribution> attributions = new LinkedHashSet<>();
    parseAttributions(attributions);
    addAdditionalAttributions(attributions);
    return attributions;
  }

  private void parseAttributions(Set<Attribution> attributions) {
    SpannableStringBuilder htmlBuilder = (SpannableStringBuilder) Html.fromHtml(attributionDataString);
    URLSpan[] urlSpans = htmlBuilder.getSpans(0, htmlBuilder.length(), URLSpan.class);
    for (URLSpan urlSpan : urlSpans) {
      parseUrlSpan(attributions, htmlBuilder, urlSpan);
    }
  }

  private void parseUrlSpan(Set<Attribution> attributions, SpannableStringBuilder htmlBuilder, URLSpan urlSpan) {
    String url = urlSpan.getURL();
    if (isUrlSpanValid(url)) {
      String title = parseAnchorValue(htmlBuilder, urlSpan);
      attributions.add(new Attribution(title, url));
    }
  }

  private boolean isUrlSpanValid(String url) {
    return isValidForImproveThisMap(url) && isValidForMapbox(url);
  }

  private boolean isValidForImproveThisMap(String url) {
    return withImproveMap || !url.equals("https://www.mapbox.com/map-feedback/");
  }

  private boolean isValidForMapbox(String url) {
    return withMapboxAttribution || !url.equals("https://www.mapbox.com/about/maps/");
  }

  private String parseAnchorValue(SpannableStringBuilder htmlBuilder, URLSpan urlSpan) {
    int start = htmlBuilder.getSpanStart(urlSpan);
    int end = htmlBuilder.getSpanEnd(urlSpan);
    int length = end - start;
    char[] charKey = new char[length];
    htmlBuilder.getChars(start, end, charKey, 0);
    return stripCopyright(String.valueOf(charKey));
  }

  private String stripCopyright(String anchor) {
    if (!withCopyrightSign && anchor.startsWith("© ")) {
      anchor = anchor.substring(2, anchor.length());
    }
    return anchor;
  }

  private void addAdditionalAttributions(Set<Attribution> attributions) {
    if (withTelemetryAttribution) {
      String telemetryKey = "Telemetry Settings";
      String telemetryLink = "https://www.mapbox.com/telemetry/";
      attributions.add(new Attribution(telemetryKey, telemetryLink));
    }
  }

  public static class Options {
    private boolean withImproveMap = true;
    private boolean withCopyrightSign = true;
    private boolean withTelemetryAttribution = false;
    private boolean withMapboxAttribution = true;
    private String[] attributionDataStringArray;

    public Options withAttributionData(String... attributionData) {
      this.attributionDataStringArray = attributionData;
      return this;
    }

    public Options withImproveMap(boolean withImproveMap) {
      this.withImproveMap = withImproveMap;
      return this;
    }

    public Options withCopyrightSign(boolean withCopyrightSign) {
      this.withCopyrightSign = withCopyrightSign;
      return this;
    }

    public Options withTelemetryAttribution(boolean withTelemetryAttribution) {
      this.withTelemetryAttribution = withTelemetryAttribution;
      return this;
    }

    public Options withMapboxAttribution(boolean withMapboxAttribution) {
      this.withMapboxAttribution = withMapboxAttribution;
      return this;
    }

    public AttributionParser build() {
      if (attributionDataStringArray == null) {
        throw new IllegalStateException("Using builder without providing attribution data");
      }

      String fullAttributionString = parseAttribution(attributionDataStringArray);
      return new AttributionParser(
        fullAttributionString,
        withImproveMap,
        withCopyrightSign,
        withTelemetryAttribution,
        withMapboxAttribution
      );
    }

    private String parseAttribution(String[] attribution) {
      StringBuilder builder = new StringBuilder();
      for (String attr : attribution) {
        if (!attr.isEmpty()) {
          builder.append(attr);
        }
      }
      return builder.toString();
    }
  }
}
