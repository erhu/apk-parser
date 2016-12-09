package cn.erhu.test.android.manifest.parser.bean;

/**
 * ActivityBean
 *
 * @author hujunjie
 * @version 1.0
 * @since 09/12/2016 6:03 PM
 */
public class ActivityBean {

    public String name;
    public IntentFilterBean intentFilter;

    @Override
    public String toString() {
        return String.format("{name:%s, intent-filter:%s}", name, intentFilter);
    }
}
