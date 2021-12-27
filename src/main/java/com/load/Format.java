package com.load;
import com.diogonunes.jcolor.Attribute;
import static com.diogonunes.jcolor.Attribute.*;

public class Format {
    final static public Attribute[] SUCCESS = {GREEN_TEXT(), BOLD()};
    final static public Attribute[] HEADING = {YELLOW_TEXT(), UNDERLINE()};
    final static public Attribute[] ERROR = {RED_TEXT()};
}
