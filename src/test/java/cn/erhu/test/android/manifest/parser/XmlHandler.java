package cn.erhu.test.android.manifest.parser;

import java.util.ArrayList;
import java.util.List;

import cn.erhu.test.android.manifest.parser.bean.ActivityBean;
import cn.erhu.test.android.manifest.parser.bean.IntentFilterBean;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XmlHandler
 *
 * @author hujunjie
 * @version 1.0
 * @since 09/12/2016 5:46 PM
 */
public class XmlHandler extends DefaultHandler {

    private ArrayList<ActivityBean> activities;
    private ArrayList<ActivityBean> services;
    private ArrayList<ActivityBean> receivers;

    private ActivityBean curActivity;
    private IntentFilterBean curIntentFilter;
    private List<String> curActions;
    private List<String> curCategories;

    public ArrayList<ActivityBean> getActivities() {
        return activities;
    }

    public ArrayList<ActivityBean> getServices() {
        return services;
    }

    public ArrayList<ActivityBean> getReceivers() {
        return receivers;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        System.out.println("----------Start Parse Document----------");
    }

    @Override
    public void endDocument() throws SAXException {
        System.out.println("----------End Parse Document----------");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        //content = new String(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        System.out.println(String.format("startElement(%s, %s)", localName, qName));

        switch (qName) {
            case "activity":
                if (activities == null) {
                    activities = new ArrayList<>();
                }
                curActivity = new ActivityBean();
                curActivity.name = attributes.getValue("android:name");
                break;

            case "service":
                if (services == null) {
                    services = new ArrayList<>();
                }
                curActivity = new ActivityBean();
                curActivity.name = attributes.getValue("android:name");
                break;

            case "receiver":
                if (receivers == null) {
                    receivers = new ArrayList<>();
                }
                curActivity = new ActivityBean();
                curActivity.name = attributes.getValue("android:name");
                break;

            case "intent-filter":
                curIntentFilter = new IntentFilterBean();
                curActivity.intentFilter = curIntentFilter;
                break;

            case "action":
                if (curActions == null) {
                    curActions = new ArrayList<>();
                }
                curActions.add(attributes.getValue("android:name"));
                break;

            case "category":
                if (curCategories == null) {
                    curCategories = new ArrayList<>();
                }
                curCategories.add(attributes.getValue("android:name"));
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);

        System.out.println(String.format("endElement(%s, %s)", localName, qName));

        switch (qName) {
            case "intent-filter":
                curIntentFilter.mActions = curActions;
                curIntentFilter.mCategories = curCategories;

                curActions = null;
                curCategories = null;
                break;
            case "activity":
                activities.add(curActivity);
                break;
            case "service":
                services.add(curActivity);
                break;
            case "receiver":
                receivers.add(curActivity);
                break;
        }
    }
}
