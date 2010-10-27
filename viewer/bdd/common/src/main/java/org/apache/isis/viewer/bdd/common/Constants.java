package org.apache.isis.viewer.bdd.common;

public final class Constants {

    private Constants() {}

    public static final String CONFIG_DIR = "config";
    public static final String LOGGING_CONFIG_FILE = "logging.properties";
    public static final String NAKEDOBJECTS_CONFIG_FILE = "nakedobjects.properties";
    
    public static final String ON_OBJECT_HEAD = "on object";
    public static final String ON_OBJECT_HEAD_ALT1 = "object";
    public static final String ON_OBJECT_HEAD_ALT2 = "on";
    public static final String[] ON_OBJECT_HEAD_SET = {
            Constants.ON_OBJECT_HEAD, Constants.ON_OBJECT_HEAD_ALT1,
            Constants.ON_OBJECT_HEAD_ALT2 };
    public static final String ON_OBJECT_NAME = Constants.ON_OBJECT_HEAD;

    public static final String ALIAS_RESULT_HEAD = "alias result as";
    public static final String ALIAS_RESULT_HEAD_ALT1 = "result=";
    public static final String ALIAS_RESULT_HEAD_ALT2 = "alias=";
    public static final String ALIAS_RESULT_HEAD_ALT3 = "alias as";
    public static final String[] ALIAS_RESULT_HEAD_SET = {
            Constants.ALIAS_RESULT_HEAD, Constants.ALIAS_RESULT_HEAD_ALT1,
            Constants.ALIAS_RESULT_HEAD_ALT2, Constants.ALIAS_RESULT_HEAD_ALT3 };
    public static final String ALIAS_RESULT_NAME = Constants.ALIAS_RESULT_HEAD;

    public static final String TITLE_NAME = "Title";
    public static final String TITLE_HEAD = "title";

    public static final String TYPE_NAME = "Type";
    public static final String TYPE_HEAD = "type";

    public static final String VALUE_NAME = "Title";
    public static final String VALUE_HEAD = "value";

    public static final String PERFORM_HEAD = "perform";
    public static final String PERFORM_HEAD_ALT1 = "do";
    public static final String PERFORM_HEAD_ALT2 = "interaction";
    public static final String PERFORM_HEAD_ALT3 = "interaction type";
    public static final String[] PERFORM_HEAD_SET = { Constants.PERFORM_HEAD,
            Constants.PERFORM_HEAD_ALT1, Constants.PERFORM_HEAD_ALT2,
            Constants.PERFORM_HEAD_ALT3 };
    public static final String PERFORM_NAME = Constants.PERFORM_HEAD;

    public static final String ON_MEMBER_HEAD = "on member";
    public static final String ON_MEMBER_HEAD_ALT1 = "using member";
    public static final String ON_MEMBER_HEAD_ALT2 = "using";
    public static final String ON_MEMBER_HEAD_ALT3 = "member";
    public static final String[] ON_MEMBER_HEAD_SET = {
            Constants.ON_MEMBER_HEAD, Constants.ON_MEMBER_HEAD_ALT1,
            Constants.ON_MEMBER_HEAD_ALT2, Constants.ON_MEMBER_HEAD_ALT3 };
    public static final String ON_MEMBER_NAME = Constants.ON_MEMBER_HEAD;

    public static final String THAT_IT_HEAD = "that it";
    public static final String THAT_IT_HEAD_ALT1 = "that";
    public static final String THAT_IT_HEAD_ALT2 = "verb";
    public static final String[] THAT_IT_HEAD_SET = { Constants.THAT_IT_HEAD,
            Constants.THAT_IT_HEAD_ALT1, Constants.THAT_IT_HEAD_ALT2 };
    public static final String THAT_IT_NAME = Constants.THAT_IT_HEAD;

    public static final String WITH_ARGUMENTS_HEAD = "with arguments";
    public static final String WITH_ARGUMENTS_HEAD_ALT1 = "with";
    public static final String WITH_ARGUMENTS_HEAD_ALT2 = "arguments";
    public static final String WITH_ARGUMENTS_HEAD_ALT3 = "parameters";
    public static final String WITH_ARGUMENTS_HEAD_ALT4 = "with parameters";
    public static final String WITH_ARGUMENTS_HEAD_ALT5 = "for";
    public static final String WITH_ARGUMENTS_HEAD_ALT6 = "for arguments";
    public static final String WITH_ARGUMENTS_HEAD_ALT7 = "for parameters";
    public static final String WITH_ARGUMENTS_HEAD_ALT8 = "value";
    public static final String WITH_ARGUMENTS_HEAD_ALT9 = "reference";
    public static final String[] WITH_ARGUMENTS_HEAD_SET = {
            Constants.WITH_ARGUMENTS_HEAD, Constants.WITH_ARGUMENTS_HEAD_ALT1,
            Constants.WITH_ARGUMENTS_HEAD_ALT2,
            Constants.WITH_ARGUMENTS_HEAD_ALT3,
            Constants.WITH_ARGUMENTS_HEAD_ALT4,
            Constants.WITH_ARGUMENTS_HEAD_ALT5,
            Constants.WITH_ARGUMENTS_HEAD_ALT6,
            Constants.WITH_ARGUMENTS_HEAD_ALT7,
            Constants.WITH_ARGUMENTS_HEAD_ALT8,
            Constants.WITH_ARGUMENTS_HEAD_ALT9 };
    public static final String WITH_ARGUMENTS_NAME = Constants.WITH_ARGUMENTS_HEAD;
}
