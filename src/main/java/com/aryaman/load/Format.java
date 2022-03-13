package com.aryaman.load;
import com.diogonunes.jcolor.Attribute;
import static com.diogonunes.jcolor.Attribute.*;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

public class Format {
    private Format() {
        throw new IllegalStateException("Format class cannot be instantiated");
    }

    public static final Attribute[] SUCCESS = {GREEN_TEXT(), BOLD()};
    public static final Attribute[] HEADING = {YELLOW_TEXT(), UNDERLINE()};
    public static final Attribute[] ERROR = {RED_TEXT()};
}
