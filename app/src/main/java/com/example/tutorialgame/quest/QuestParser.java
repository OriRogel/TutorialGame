package com.example.tutorialgame.quest;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.example.tutorialgame.managers.worldactions.ActionFactory;
import com.example.tutorialgame.managers.worldactions.WorldAction;
import com.example.tutorialgame.managers.worldactions.WorldEvent;
import com.example.tutorialgame.engine.interfaces.StateSwitcher;
import com.example.tutorialgame.ui.base.BaseActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses quests from an XML file.
 */
public class QuestParser {
    private static final String TAG = "QuestParser";

    public static List<ComplexQuest> parseQuests(Context context, String fileName, StateSwitcher switcher) {
        ActionFactory actionFactory = new ActionFactory(switcher);
        List<ComplexQuest> mainStoryLine = new ArrayList<>();
        try (InputStream is = context.getAssets().open(fileName)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            int eventType = parser.getEventType();
            ComplexQuest currentComplexQuest = null;
            List<Quest> currentSubQuests = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("quest".equals(name)) {
                            int titleRes = getResId(context, parser.getAttributeValue(null, "title_res"), "string");
                            currentSubQuests = new ArrayList<>();
                            currentComplexQuest = new ComplexQuest(titleRes, currentSubQuests);
                        } else if ("step".equals(name)) {
                            Quest step = parseStep(context, parser, actionFactory);
                            if (currentSubQuests != null) currentSubQuests.add(step);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("quest".equals(name) && currentComplexQuest != null) {
                            mainStoryLine.add(currentComplexQuest);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Error parsing quests XML", e);
        }
        return mainStoryLine;
    }

    private static Quest parseStep(Context context, XmlPullParser parser, ActionFactory actionFactory) throws IOException, XmlPullParserException {
        String id = parser.getAttributeValue(null, "id");
        int taskRes = getResId(context, parser.getAttributeValue(null, "task_res"), "string");
        int coins = getIntAttribute(parser, "coins", 0);
        int xp = getIntAttribute(parser, "xp", 0);

        QuestType questType = null;
        WorldEvent onComplete = new WorldEvent();

        int eventType = parser.next();
        while (!(eventType == XmlPullParser.END_TAG && "step".equals(parser.getName()))) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if ("requirement".equals(name)) {
                    String typeStr = parser.getAttributeValue(null, "type");
                    String target = parser.getAttributeValue(null, "target");
                    questType = new QuestType(QuestType.Type.valueOf(typeStr), target);
                } else if ("onSuccess".equals(name)) {
                    parseActions(parser, onComplete, actionFactory);
                }
            }
            eventType = parser.next();
        }

        return new Quest(taskRes, id, questType, coins, xp, onComplete::trigger);
    }

    private static void parseActions(XmlPullParser parser, WorldEvent event, ActionFactory actionFactory) throws IOException, XmlPullParserException {
        int eventType = parser.next();
        while (!(eventType == XmlPullParser.END_TAG && "onSuccess".equals(parser.getName()))) {
            if (eventType == XmlPullParser.START_TAG && "action".equals(parser.getName())) {
                String type = parser.getAttributeValue(null, "type");
                Map<String, String> params = new HashMap<>();
                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    String attrName = parser.getAttributeName(i);
                    if (!"type".equals(attrName)) {
                        params.put(attrName, parser.getAttributeValue(i));
                    }
                }
                
                // Handle music resource mapping
                if (params.containsKey("music")) {
                    params.put("musicRes", String.valueOf(getResId(BaseActivity.getContext(), params.get("music"), "raw")));
                }

                WorldAction action = actionFactory.createAction(type, params);
                if (action != null) event.addAction(action);
            }
            eventType = parser.next();
        }
    }

    private static int getResId(Context context, String resName, String defType) {
        if (resName == null || resName.isEmpty()) return 0;
        return context.getResources().getIdentifier(resName, defType, context.getPackageName());
    }

    private static int getIntAttribute(XmlPullParser parser, String attrName, int defaultValue) {
        String val = parser.getAttributeValue(null, attrName);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }
}
