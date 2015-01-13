package com.lldogg.avrz2irp;

import android.text.Html;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Garrick on 1/10/15.
 */
public class MyXMLparser {
    // We don't use namespaces
    private static final String ns = null;

    static public String parse(InputStream in) throws XmlPullParserException, IOException {
        System.out.printf("%s", "entered parse()\n");

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(in, null);

            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    static private String readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        //System.out.printf("%s", "entered readFeed()\n");
        int valueflags[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        String values[] = {"", "", "", "", "", "", "", "", "", "", ""};
        int count;
        int menu = 0;
        String output="";
        String NetPlayingTitle = "";
        String NetFuncSelect = "";

        parser.require(XmlPullParser.START_TAG, ns, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("szLine")) {
                //System.out.printf("%s", "found szLine\n");
                values = readszLine(parser);
            } else if (name.equals("chFlag")) {
                //System.out.printf("%s", "found chFlag\n");
                valueflags = readchFlag(parser);
            } else if (name.equals("NetPlayingTitle")) {
                NetPlayingTitle = readNetPlayingTitle(parser);
            } else if (name.equals("NetFuncSelect")) {
                NetFuncSelect = readNetFuncSelect(parser);
            } else {
                skip(parser);
            }
        }

        // at this point, we have 10 valueflags and 10 values
        // we need to look through them and format the output
        for (count = 0; count <= 9; count++) {
            if (valueflags[count] == 9) {
                // this is a menu, the player is stopped (is this true for iradio?)
                menu = 1;
            }
        }

        //if (NetPlayingTitle.equals("")) {
        //    return "Net radio is currently off. Push one of the buttons below.";
        //}

        if (menu == 1) {

            for (count = 0; count <= 9; count++) {
                if (valueflags[count]==0) {
                    continue; // it has a bogus value here
                } else if ((valueflags[count]==1) && (values[count].length() >= 0)) {
                    output += " *"+Html.fromHtml(values[count]).toString() + "\n";
                } else if ((valueflags[count]==9) && (values[count].length() >= 0)) {
                    output += ">*"+Html.fromHtml(values[count]).toString() + "\n";
                }
            }

        } else {
            if ((NetFuncSelect.equals("FAVORITES")) || (NetFuncSelect.equals("IRADIO"))) {
                output += " - Favorites - \n";
                for (count = 0; count <= 9; count++) {
                    if (count == 5) {
                        continue; // it has a bogus value here
                    } else if (values[count].length() >= 0) {
                        output += Html.fromHtml(values[count]).toString() + "\n";
                    }
                }
            } else if (NetFuncSelect.equals("PANDORA")) {
                output += " - Pandora - \n";
                output += " - Station: " + NetPlayingTitle + "\n";
                for (count = 0; count <= 9; count++) {
                    if (count == 5) {
                        continue;
                    } else if (values[count].length() >= 0) {
                        output += Html.fromHtml(values[count]).toString() + "\n";
                    }

                }
            }
        }
        return output;
    }

    static private int[] readchFlag(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "chFlag");
        int[] values = new int[10];
        int count = 0;
        //System.out.printf("%s", "entered readchFlag()\n");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("value")) {
                //System.out.printf("%s", "found chFlag value\n");
                if (count > 9) {
                    skip(parser);
                } else {
                    values[count] = Integer.parseInt(readValue(parser));
                    count++;
                }
            } else {
                skip(parser);
            }
        }
        return values;
    }

    static private String[] readszLine(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "szLine");
        String values[] = new String[10];
        int count = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("value")) {
                if (count > 9) {
                    skip(parser);
                } else {
                    values[count] = readValue(parser);
                    count++;
                }

            } else {
                skip(parser);
            }
        }
        return values;
    }

    static private String readNetPlayingTitle(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "NetPlayingTitle");
        String output = "";

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("value")) {
                output = readValue(parser);

            } else {
                skip(parser);
            }
        }
        return output;
    }

    static private String readNetFuncSelect(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "NetFuncSelect");
        String output = "";

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("value")) {
                output = readValue(parser);

            } else {
                skip(parser);
            }
        }
        return output;
    }

    static private String readValue(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "value");
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "value");
        return value;
    }

    static private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    static private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
