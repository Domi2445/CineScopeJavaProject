package com.filmeverwaltung.javaprojektfilmverwaltung.model;

/**
 * Model für die MyMemory Translation API Response
 */
public class TranslationResponse
{

    private ResponseData responseData;
    private String responseStatus;
    private String responseDetails;
    private Matches[] matches;

    public ResponseData getResponseData()
    {
        return responseData;
    }

    public void setResponseData(ResponseData responseData)
    {
        this.responseData = responseData;
    }

    public String getResponseStatus()
    {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus)
    {
        this.responseStatus = responseStatus;
    }

    public String getResponseDetails()
    {
        return responseDetails;
    }

    public void setResponseDetails(String responseDetails)
    {
        this.responseDetails = responseDetails;
    }

    public Matches[] getMatches()
    {
        return matches;
    }

    public void setMatches(Matches[] matches)
    {
        this.matches = matches;
    }

    /**
     * Innere Klasse für die eigentlichen Übersetzungsdaten
     */
    public static class ResponseData
    {
        private String translatedText;
        private double match;

        public String getTranslatedText()
        {
            return translatedText;
        }

        public void setTranslatedText(String translatedText)
        {
            this.translatedText = translatedText;
        }

        public double getMatch()
        {
            return match;
        }

        public void setMatch(double match)
        {
            this.match = match;
        }
    }

    /**
     * Innere Klasse für alternative Übersetzungen
     */
    public static class Matches
    {
        private String translation;
        private String source;
        private String target;
        private double quality;
        private String reference;

        public String getTranslation()
        {
            return translation;
        }

        public void setTranslation(String translation)
        {
            this.translation = translation;
        }

        public String getSource()
        {
            return source;
        }

        public void setSource(String source)
        {
            this.source = source;
        }

        public String getTarget()
        {
            return target;
        }

        public void setTarget(String target)
        {
            this.target = target;
        }

        public double getQuality()
        {
            return quality;
        }

        public void setQuality(double quality)
        {
            this.quality = quality;
        }

        public String getReference()
        {
            return reference;
        }

        public void setReference(String reference)
        {
            this.reference = reference;
        }
    }
}

