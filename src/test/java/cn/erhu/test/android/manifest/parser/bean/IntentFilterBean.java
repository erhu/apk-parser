package cn.erhu.test.android.manifest.parser.bean;

import java.util.List;

/**
 * IntentFilterBean
 *
 * @author hujunjie
 * @version 1.0
 * @since 09/12/2016 6:03 PM
 */
public class IntentFilterBean {

    public List<String> mActions;
    public List<String> mCategories;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        if (mActions != null && !mActions.isEmpty()) {
            builder.append("actions:[");
            for (String a : mActions) {
                builder.append(a).append(",");
            }
            builder.append("]");
        }
        if (mCategories != null && !mCategories.isEmpty()) {
            builder.append(", categories:[");
            for (String a : mCategories) {
                builder.append(a).append(",");
            }
            builder.append("]");
        }
        builder.append("}");
        return builder.toString();
    }
}
