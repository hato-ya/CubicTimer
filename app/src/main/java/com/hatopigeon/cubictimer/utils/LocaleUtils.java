package com.hatopigeon.cubictimer.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Pair;

import androidx.preference.PreferenceManager;

import com.hatopigeon.cubicify.R;
import com.hatopigeon.cubictimer.CubicTimer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Utility class to facilitate locale customization by the user
 * Modified from http://gunhansancar.com/change-language-programmatically-in-android/
 */
public class LocaleUtils {

    // The codes used are alpha-2 ISO 639-1, followed by underline
    // and alpha-2 ISO 3166 country/subdivision code if necessary.

    // English is separated into "normal" and "USA" since America has its own date format
    public static final String CZECH             = "cs_CZ";
    public static final String CHINESE           = "zh_CN";
    public static final String CHINESE_HK        = "zh_HK";
    public static final String CHINESE_TW        = "zh_TW";
    public static final String CATALAN           = "ca_ES";
    public static final String HUNGARIAN         = "hu_HU";
    public static final String VIETNAMESE        = "vi_VN";
    public static final String ARABIC            = "ar_SA";
    public static final String TAMIL             = "ta_IN";
    public static final String INDONESIAN        = "id_ID";
    public static final String HEBREW            = "he_IL";
    public static final String DUTCH             = "nl_NL";
    public static final String SWEDISH           = "sv_SE";
    public static final String VALENCIAN         = "val_ES";
    public static final String ITALIAN           = "it_IT";
    public static final String ESPERANTO         = "eo";
    public static final String ENGLISH           = "en_GB";
    public static final String ENGLISH_USA       = "en_US";
    public static final String SPANISH           = "es_ES";
    public static final String GERMAN            = "de_DE";
    public static final String FRENCH            = "fr_FR";
    public static final String RUSSIAN           = "ru_RU";
    public static final String PORTUGUESE_BRAZIL = "pt_BR";
    public static final String LITHUANIAN        = "lt_LT";
    public static final String POLISH            = "pl_PL";
    public static final String SERBIAN_CYRILLIC  = "sr_Cyrl_RS";
    public static final String SERBIAN_LATIN     = "sr_Latn_RS";
    public static final String CROATIAN          = "hr_HR";
    public static final String TURKISH           = "tr_TR";
    public static final String SLOVAK            = "sk_SK";
    public static final String JAPANESE          = "ja_JP";
    public static final String HINDI             = "hi_IN";
    public static final String UKRAINIAN         = "uk_UA";
    public static final String BELARUSIAN        = "be_BY";
    public static final String AFRIKAANS         = "af_ZA";
    public static final String DANISH            = "da_DK";
    public static final String GREEK             = "el_GR";
    public static final String BASQUE            = "eu_ES";
    public static final String FINNISH           = "fi_FI";
    public static final String KOREAN            = "ko_KR";
    public static final String NORWEGIAN         = "no_NO";
    public static final String ROMANIAN          = "ro_RO";

    public static Context updateLocale(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = prefs.getString(context.getString(R.string.pk_locale), Locale.getDefault().toString());

        return updateResources(context, language);
    }

    /**
     * Gets current locale
     *
     * @return the locale
     */
    public static String getLocale() {
        return Prefs.getString(R.string.pk_locale, Locale.getDefault().getLanguage());
    }

    /**
     * Gets current locale
     *
     * @return the locale
     */
    public static void resetLocale() {
        Prefs.edit().remove(R.string.pk_locale).apply();
    }

    private static LinkedHashMap<String, Integer> localeHash = null;

    /**
     * Returns a HashMap containing {@link Pair} with each language name and flag resource ID.
     * The languages are keyed by the locale code
     */
    public static LinkedHashMap<String, Integer> getLocaleHashMap() {
        if (localeHash == null) {
            localeHash = new LinkedHashMap<String, Integer>() {
                {
                    put(ENGLISH_USA, R.string.language_english_usa);
                    put(ENGLISH,     R.string.language_english);

                    put(AFRIKAANS,   R.string.language_afrikaans);
                    put(ARABIC,      R.string.language_arabic);
                    put(BASQUE,      R.string.language_basque);
                    put(BELARUSIAN,  R.string.language_belarusian);
                    put(CATALAN,     R.string.language_catalan);
                    put(CHINESE,     R.string.language_chinese);
                    put(CHINESE_HK,  R.string.language_chinese_hk);
                    put(CHINESE_TW,  R.string.language_chinese_tw);
                    put(CROATIAN,    R.string.language_croatian);
                    put(CZECH,       R.string.language_czech);
                    put(DANISH,      R.string.language_danish);
                    put(DUTCH,       R.string.language_dutch);
                    put(ESPERANTO,   R.string.language_esperanto);
                    put(FINNISH,     R.string.language_finnish);
                    put(FRENCH,      R.string.language_french);
                    put(GERMAN,      R.string.language_german);
                    put(GREEK,       R.string.language_greek);
                    put(HEBREW,      R.string.language_hebrew);
                    put(HINDI,       R.string.language_hindi);
                    put(HUNGARIAN,   R.string.language_hungarian);
                    put(INDONESIAN,  R.string.language_indonesian);
                    put(ITALIAN,     R.string.language_italian);
                    put(JAPANESE,    R.string.language_japanese);
                    put(KOREAN,      R.string.language_korean);
                    put(LITHUANIAN,  R.string.language_lithuanian);
                    put(NORWEGIAN,   R.string.language_norwegian);
                    put(POLISH,      R.string.language_polish);
                    put(PORTUGUESE_BRAZIL, R.string.language_portuguese_br);
                    put(ROMANIAN,    R.string.language_romanian);
                    put(RUSSIAN,     R.string.language_russian);
                    put(SERBIAN_CYRILLIC, R.string.language_serbian_cyrillic);
                    put(SERBIAN_LATIN,    R.string.language_serbian_latin);
                    put(SLOVAK,      R.string.language_slovak);
                    put(SPANISH,     R.string.language_spanish);
                    put(SWEDISH,     R.string.language_swedish);
                    put(TAMIL,       R.string.language_tamil);
                    put(TURKISH,     R.string.language_turkish);
                    put(UKRAINIAN,   R.string.language_ukrainian);
                    put(VALENCIAN,   R.string.language_valencian);
                    put(VIETNAMESE,  R.string.language_vietnamese);
                }
            };
        }
        return localeHash;
    }

    /**
     * Returns an array with all available locale codes
     * @return
     */
    public static String[] getLocaleArray() {
        return (String[]) getLocaleHashMap().keySet().toArray(new String[0]);
    }

    /**
     * Sets current locale
     *
     * @param language the language code (use one of the constants)
     */
    public static void setLocale(String language) {
        Prefs.edit().putString(R.string.pk_locale, language).apply();
        updateLocale(CubicTimer.getAppContext());
    }


    private static Context updateResources(Context context, String language) {
        Locale locale = fetchLocaleFromString(language);
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    /**
     * Converts a locale string into BCP47 format
     * @param language
     * @return
     */
    private static Locale fetchLocaleFromString(String language) {
        language = language.replace('_', '-'); // convert to BCP47 format
        return Locale.forLanguageTag(language);
    }
}