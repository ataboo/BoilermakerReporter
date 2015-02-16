package com.atasoft.boilermakerreporter;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;


public class AboutPage extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_page);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        addLinks();
    }

    private void addLinks(){
        TextView aboutText = (TextView) findViewById(R.id.aboutblurb);
        String aboutString = getResources().getString(R.string.about_blurb);
        aboutString = aboutString.replace("Boilermaker Toolbox", "<a href=\"https://play.google.com/store/apps/details?id=com.atasoft.flangeassist\">Boilermaker Toolbox</a>");
        aboutString = aboutString.replace("bmtoolbox@gmail.com", "<a href=\"mailto:bmtoolbox@gmail.com\">bmtoolbox@gmail.com</a>");
        aboutString = aboutString.replace("PDFBox-Android", "<a href=\"https://github.com/Birdbrain2/PdfBox-Android\">PDFBox-Android</a>");
        aboutString = aboutString.replace("@LINEBREAK", "<br><br>");
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
        Log.w("AboutPage", aboutString);
        aboutText.setText(Html.fromHtml(aboutString));
    }
}
