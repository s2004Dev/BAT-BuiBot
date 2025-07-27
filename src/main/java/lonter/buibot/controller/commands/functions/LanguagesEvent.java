package lonter.buibot.controller.commands.functions;

import lombok.AllArgsConstructor;

import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public enum LanguagesEvent {
  AFRIKAANS("Afrikaans"),
  ALBANIAN("Albanian"),
  ARABIC("Arabic"),
  BELARUSIAN("Byelorussian"),
  BENGALI("Bengali"),
  BOSNIAN("Bosnian"),
  BULGARIAN("Bulgarian"),
  CATALAN("Catalan"),
  CHINESE("Chinese"),
  CROATIAN("Croatian"),
  CZECH("Czech"),
  DANISH("Danish"),
  DUTCH("Dutch"),
  ESPERANTO("Esperanto", "https://en.bab.la/dictionary/esperanto-english/"),
  FINNISH("Finnish"),
  FRENCH("French"),
  GEORGIAN("Georgian"),
  GERMAN("German"),
  GREEK("Greek", "https://www.lexilogos.com/english/greek_modern_dictionary.htm"),
  HINDI("Hindi"),
  HUNGARIAN("Hungarian"),
  ICELANDIC("Icelandic"),
  INDONESIAN("Indonesian"),
  IRISH("Irish", "https://www.focloir.ie/en/browse/ei/"),
  ITALIAN("Italian"),
  JAPANESE("Japanese"),
  KOREAN("Korean"),
  LATIN("Latin"),
  LITHUANIAN("Lithuanian"),
  MALTESE("Maltese"),
  MONGOLIAN("Mongolian"),
  NAEPOLITAN("Neapolitan", "https://glosbe.com/nap/en"),
  NEPALI("Nepali"),
  NORWEGIAN("Norwegian"),
  OCCITAN("Occitan", "https://glosbe.com/oc/en"),
  PERSIAN("Persian"),
  POLISH("Polish"),
  PORTUGUESE("Portuguese"),
  ROMANIAN("Romanian"),
  RUSSIAN("Russian"),
  SCOTTISH("Scottish", "https://dsl.ac.uk/"),
  SERBIAN("Serbian"),
  SLOVAK("Slovak"),
  SLOVENIAN("Slovenian"),
  SPANISH("Spanish"),
  SWEDISH("Swedish"),
  TAGALOG("Tagalog"),
  THAI("Thai"),
  TIBETAN("Tibetan"),
  TURKISH("Turkish"),
  UKRAINIAN("Ukrainian"),
  VIETNAMESE("Vietnamese"),
  WELSH("Welsh");

  public final String name;
  public final String wiki;

//  private static StringBuilder lines;

  LanguagesEvent(final @NotNull String name) {
    this.name = name;
    this.wiki = "https://www.lexilogos.com/english/" + name.toLowerCase() + "_dictionary.htm";
  }

//  public static @Nullable LanguagesEvent getFromString(final @NotNull String value) {
//    for(val i: values())
//      if(i.name.equalsIgnoreCase(value))
//        return i;
//
//    return null;
//  }

//  public static LanguagesEvent getRandom() {
//    lines = new StringBuilder();
//    LanguagesEvent lang;
//
//    try(var input = new Scanner(new File(path))) {
//      lang = getFromString(input.nextLine());
//
//      while(input.hasNextLine())
//        lines.append(input.nextLine()).append("\n");
//    }
//
//    catch(Exception e) {
//      val languages = new ArrayList<>(Arrays.asList(values()));
//
//      Collections.shuffle(languages);
//      lang = languages.getFirst();
//      languages.remove(lang);
//
//      languages.forEach(i ->
//        lines.append(i).append("\n"));
//    }
//
//    return lang;
//  }

  @Override
  public String toString() {
    return name;
  }
}