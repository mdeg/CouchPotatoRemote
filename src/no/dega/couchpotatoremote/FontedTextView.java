package no.dega.couchpotatoremote;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Simple custom implementation of TextView to allow the use of CouchPotato's font, Open Sans, everywhere
 */
public class FontedTextView extends TextView {
    public FontedTextView(Context context) {
        super(context);
        init(context);
    }

    public FontedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FontedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");
        this.setTypeface(font);
    }
}
