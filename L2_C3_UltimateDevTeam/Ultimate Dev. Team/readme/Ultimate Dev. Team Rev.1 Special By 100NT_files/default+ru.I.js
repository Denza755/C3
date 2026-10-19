(function() {
var _UDS_CONST_LOCALE = 'ru';
var _UDS_CONST_SHORT_DATE_PATTERN = 'DMY';
var _UDS_MSG_SEARCHER_IMAGE = ('\u041a\u0430\u0440\u0442\u0438\u043d\u043a\u0430');
var _UDS_MSG_SEARCHER_WEB = ('\u0418\u043d\u0442\u0435\u0440\u043d\u0435\u0442');
var _UDS_MSG_SEARCHER_BLOG = ('\u0411\u043b\u043e\u0433');
var _UDS_MSG_SEARCHER_VIDEO = ('\u0412\u0438\u0434\u0435\u043e');
var _UDS_MSG_SEARCHER_LOCAL = ('\u0410\u0434\u0440\u0435\u0441\u0430');
var _UDS_MSG_SEARCHCONTROL_SAVE = ('\u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c');
var _UDS_MSG_SEARCHCONTROL_KEEP = ('\u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c');
var _UDS_MSG_SEARCHCONTROL_INCLUDE = ('\u0432\u043a\u043b\u044e\u0447\u0438\u0442\u044c');
var _UDS_MSG_SEARCHCONTROL_COPY = ('\u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c');
var _UDS_MSG_SEARCHCONTROL_CLOSE = ('\u0437\u0430\u043a\u0440\u044b\u0442\u044c');
var _UDS_MSG_SEARCHCONTROL_SPONSORED_LINKS = ('\u0420\u0435\u043a\u043b\u0430\u043c\u043d\u044b\u0435 \u0441\u0441\u044b\u043b\u043a\u0438');
var _UDS_MSG_SEARCHCONTROL_SEE_MORE = ('\u0435\u0449\u0451...');
var _UDS_MSG_SEARCHCONTROL_WATERMARK = ('\u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u0438\u0437 Google');
var _UDS_MSG_SEARCHER_CONFIG_SET_LOCATION = ('\u0420\u0435\u0433\u0438\u043e\u043d \u043f\u043e\u0438\u0441\u043a\u0430');
var _UDS_MSG_SEARCHER_CONFIG_DISABLE_ADDRESS_LOOKUP = ('\u041e\u0442\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u043f\u043e\u0438\u0441\u043a \u0430\u0434\u0440\u0435\u0441\u043e\u0432');
var _UDS_MSG_SEARCHER_NEWS = ('\u041d\u043e\u0432\u043e\u0441\u0442\u0438');
function _UDS_MSG_MINUTES_AGO(AGE_MINUTES_AGO) {return ('' + AGE_MINUTES_AGO + ' \u043c\u0438\u043d. \u043d\u0430\u0437\u0430\u0434');}
var _UDS_MSG_ONE_HOUR_AGO = ('1 \u0447\u0430\u0441 \u043d\u0430\u0437\u0430\u0434');
function _UDS_MSG_HOURS_AGO(AGE_HOURS_AGO) {return ('' + AGE_HOURS_AGO + ' \u0447. \u043d\u0430\u0437\u0430\u0434');}
function _UDS_MSG_NEWS_ALL_N_RELATED(NUMBER) {return ('\u043f\u043e\u0445\u043e\u0436\u0438\u0435 \u0441\u0442\u0430\u0442\u044c\u0438: ' + NUMBER);}
var _UDS_MSG_NEWS_RELATED = ('\u041f\u043e\u0445\u043e\u0436\u0438\u0435 \u0441\u0442\u0430\u0442\u044c\u0438');
var _UDS_MSG_BRANDING_STRING = ('\u0442\u0435\u0445\u043d\u043e\u043b\u043e\u0433\u0438\u0438 Google');
var _UDS_MSG_SORT_BY_DATE = ('\u0423\u043f\u043e\u0440\u044f\u0434\u043e\u0447\u0438\u0442\u044c \u043f\u043e \u0434\u0430\u0442\u0435');
var _UDS_MSG_MONTH_ABBR_JAN = ('\u044f\u043d\u0432');
var _UDS_MSG_MONTH_ABBR_FEB = ('\u0444\u0435\u0432');
var _UDS_MSG_MONTH_ABBR_MAR = ('\u043c\u0430\u0440');
var _UDS_MSG_MONTH_ABBR_APR = ('\u0430\u043f\u0440');
var _UDS_MSG_MONTH_ABBR_MAY = ('\u043c\u0430\u0439');
var _UDS_MSG_MONTH_ABBR_JUN = ('\u0438\u044e\u043d');
var _UDS_MSG_MONTH_ABBR_JUL = ('\u0438\u044e\u043b');
var _UDS_MSG_MONTH_ABBR_AUG = ('\u0430\u0432\u0433');
var _UDS_MSG_MONTH_ABBR_SEP = ('\u0441\u0435\u043d\u0442');
var _UDS_MSG_MONTH_ABBR_OCT = ('\u043e\u043a\u0442');
var _UDS_MSG_MONTH_ABBR_NOV = ('\u043d\u043e\u044f\u0431');
var _UDS_MSG_MONTH_ABBR_DEC = ('\u0434\u0435\u043a');
var _UDS_MSG_DIRECTIONS = ('\u043c\u0430\u0440\u0448\u0440\u0443\u0442\u044b \u043f\u0440\u043e\u0435\u0437\u0434\u0430');
var _UDS_MSG_CLEAR_RESULTS = ('\u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b');
var _UDS_MSG_SHOW_ONE_RESULT = ('\u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u043e\u0434\u0438\u043d \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442');
var _UDS_MSG_SHOW_MORE_RESULTS = ('\u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0434\u0440\u0443\u0433\u0438\u0435 \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b');
var _UDS_MSG_SHOW_ALL_RESULTS = ('\u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0432\u0441\u0435 \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b');
var _UDS_MSG_SETTINGS = ('\u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438');
var _UDS_MSG_SEARCH = ('\u043f\u043e\u0438\u0441\u043a');
var _UDS_MSG_SEARCH_UC = ('\u041f\u043e\u0438\u0441\u043a');
var _UDS_MSG_POWERED_BY = ('\u0442\u0435\u0445\u043d\u043e\u043b\u043e\u0433\u0438\u044f');
function _UDS_MSG_LOCAL_ATTRIBUTION(LOCAL_RESULTS_PROVIDER) {return ('\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a \u0441\u0432\u0435\u0434\u0435\u043d\u0438\u0439 \u043e \u0431\u0438\u0437\u043d\u0435\u0441\u0430\u0445:  ' + LOCAL_RESULTS_PROVIDER);}
var _UDS_MSG_SEARCHER_BOOK = ('\u041a\u043d\u0438\u0433\u0438');
function _UDS_MSG_FOUND_ON_PAGE(FOUND_ON_PAGE) {return ('\u0421\u0442\u0440\u0430\u043d\u0438\u0446\u0430 ' + FOUND_ON_PAGE);}
function _UDS_MSG_TOTAL_PAGE_COUNT(PAGE_COUNT) {return ('' + PAGE_COUNT + ' \u0441\u0442\u0440.');}
var _UDS_MSG_SEARCHER_BY = ('\u0410\u0432\u0442\u043e\u0440:');
var _UDS_MSG_SEARCHER_CODE = ('\u041a\u043e\u0434');
var _UDS_MSG_UNKNOWN_LICENSE = ('\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043b\u0438\u0446\u0435\u043d\u0437\u0438\u044f');
var _UDS_MSG_SEARCHER_GSA = ('Google Search Appliance');
var _UDS_MSG_SEARCHCONTROL_MORERESULTS = ('\u0414\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u044b\u0435 \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b');
var _UDS_MSG_SEARCHCONTROL_PREVIOUS = ('\u041f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0430\u044f');
var _UDS_MSG_SEARCHCONTROL_NEXT = ('\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f');
var _UDS_MSG_GET_DIRECTIONS = ('\u041c\u0430\u0440\u0448\u0440\u0443\u0442\u044b \u043f\u0440\u043e\u0435\u0437\u0434\u0430');
var _UDS_MSG_GET_DIRECTIONS_TO_HERE = ('\u0421\u044e\u0434\u0430');
var _UDS_MSG_GET_DIRECTIONS_FROM_HERE = ('\u041e\u0442\u0441\u044e\u0434\u0430');
var _UDS_MSG_CLEAR_RESULTS_UC = ('\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b');
var _UDS_MSG_SEARCH_THE_MAP = ('\u043d\u0430\u0439\u0442\u0438 \u043d\u0430 \u043a\u0430\u0440\u0442\u0435');
var _UDS_MSG_SCROLL_THROUGH_RESULTS = ('\u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b');
var _UDS_MSG_EDIT_TAGS = ('\u0438\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u0442\u0435\u0433\u0438');
var _UDS_MSG_TAG_THIS_SEARCH = ('\u043e\u0442\u043c\u0435\u0442\u0438\u0442\u044c \u044d\u0442\u043e\u0442 \u043f\u043e\u0438\u0441\u043a');
var _UDS_MSG_SEARCH_STRING = ('\u043d\u0430\u0439\u0442\u0438 \u0446\u0435\u043f\u043e\u0447\u043a\u0443');
var _UDS_MSG_OPTIONAL_LABEL = ('\u0434\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u044f\u0440\u043b\u044b\u043a');
var _UDS_MSG_DELETE = ('\u0443\u0434\u0430\u043b\u0438\u0442\u044c');
var _UDS_MSG_DELETED = ('\u0443\u0434\u0430\u043b\u0435\u043d\u043e');
var _UDS_MSG_CANCEL = ('\u043e\u0442\u043c\u0435\u043d\u0430');
var _UDS_MSG_UPLOAD_YOUR_VIDEOS = ('\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u0435 \u0441\u043e\u0431\u0441\u0442\u0432\u0435\u043d\u043d\u044b\u0439 \u0432\u0438\u0434\u0435\u043e\u0440\u043e\u043b\u0438\u043a');
var _UDS_MSG_IM_DONE_WATCHING = ('\u0437\u0430\u043a\u043e\u043d\u0447\u0438\u0442\u044c \u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440');
var _UDS_MSG_CLOSE_VIDEO_PLAYER = ('\u0437\u0430\u043a\u0440\u044b\u0442\u044c \u0432\u0438\u0434\u0435\u043e\u043f\u043b\u0435\u0435\u0440');
var _UDS_MSG_NO_RESULTS = ('\u0420\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u043e\u0432 \u043d\u0435\u0442');
var _UDS_MSG_LINKEDCSE_ERROR_RESULTS = ('\u0412 \u0434\u0430\u043d\u043d\u044b\u0439 \u043c\u043e\u043c\u0435\u043d\u0442 \u044d\u0442\u0430 \u0441\u0438\u0441\u0442\u0435\u043c\u0430 \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044c\u0441\u043a\u043e\u0433\u043e \u043f\u043e\u0438\u0441\u043a\u0430 \u0437\u0430\u0433\u0440\u0443\u0436\u0430\u0435\u0442\u0441\u044f. \u041f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u0435 \u043f\u043e\u043f\u044b\u0442\u043a\u0443 \u0447\u0435\u0440\u0435\u0437 \u043d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e \u0441\u0435\u043a\u0443\u043d\u0434.');
var _UDS_MSG_COUPONS = ('\u041a\u0443\u043f\u043e\u043d\u044b');
var _UDS_MSG_BACK = ('\u043d\u0430\u0437\u0430\u0434');
var _UDS_MSG_SUBSCRIBE = ('\u041f\u043e\u0434\u043f\u0438\u0441\u0430\u0442\u044c\u0441\u044f');
var _UDS_MSG_SEARCHER_PATENT = ('Patent');
var _UDS_MSG_USPAT = ('\u041f\u0430\u0442\u0435\u043d\u0442 \u043d\u0430 \u0442\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u0438 \u0421\u0428\u0410.');
var _UDS_MSG_USPAT_APP = ('\u0417\u0430\u044f\u0432\u043a\u0430 \u043d\u0430 \u043f\u0430\u0442\u0435\u043d\u0442 \u0432 \u0421\u0428\u0410');
var _UDS_MSG_PATENT_FILED = ('\u041f\u043e\u043c\u0435\u0449\u0435\u043d\u043e');
var _UDS_MSG_ADS_BY_GOOGLE = ('\u0420\u0435\u043a\u043b\u0430\u043c\u0430 \u043e\u0442 Google');
var _UDS_MSG_SET_DEFAULT_LOCATION = ('\u0417\u0430\u0434\u0430\u0439\u0442\u0435 \u043c\u0435\u0441\u0442\u043e\u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e');
var _UDS_MSG_NEWSCAT_TOPSTORIES = ('\u0413\u043b\u0430\u0432\u043d\u044b\u0435 \u043d\u043e\u0432\u043e\u0441\u0442\u0438');
var _UDS_MSG_NEWSCAT_WORLD = ('\u0412 \u043c\u0438\u0440\u0435');
var _UDS_MSG_NEWSCAT_NATION = ('\u041d\u0430\u0446\u0438\u044f');
var _UDS_MSG_NEWSCAT_BUSINESS = ('\u0411\u0438\u0437\u043d\u0435\u0441');
var _UDS_MSG_NEWSCAT_SCITECH = ('\u041d\u0430\u0443\u043a\u0430 \u0438 \u0442\u0435\u0445\u043d\u0438\u043a\u0430');
var _UDS_MSG_NEWSCAT_ENTERTAINMENT = ('\u041a\u0443\u043b\u044c\u0442\u0443\u0440\u0430');
var _UDS_MSG_NEWSCAT_HEALTH = ('\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435');
var _UDS_MSG_NEWSCAT_SPORTS = ('\u0421\u043f\u043e\u0440\u0442');
var _UDS_MSG_NEWSCAT_POLITICS = ('\u041f\u043e\u043b\u0438\u0442\u0438\u043a\u0430');
var _UDS_MSG_SEARCH_RESULTS = ('\u0420\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b \u043f\u043e\u0438\u0441\u043a\u0430');
var _UDS_MSG_DID_YOU_MEAN = ('\u0412\u043e\u0437\u043c\u043e\u0436\u043d\u043e, \u0432\u044b \u0438\u043c\u0435\u043b\u0438 \u0432 \u0432\u0438\u0434\u0443:');
var _UDS_MSG_CUSTOM_SEARCH = ('\u041f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044c\u0441\u043a\u0438\u0439 \u043f\u043e\u0438\u0441\u043a');
var _UDS_MSG_LABELED = ('\u0421 \u043f\u043e\u043c\u0435\u0442\u043a\u043e\u0439');
var _UDS_MSG_LOADING = ('\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430...');
var _UDS_MSG_ALL_RESULTS_SHORT = ('\u0412\u0441\u0435');
var _UDS_MSG_ALL_RESULTS_LONG = ('\u0412\u0441\u0435 \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b');
var _UDS_MSG_REFINE_RESULTS = ('\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u044c \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b:');
function _UDS_MSG_REVIEWS(REVIEW_COUNT) {return ('\u041e\u0442\u0437\u044b\u0432\u043e\u0432: ' + REVIEW_COUNT);}
function _UDS_MSG_CALORIES(CALORIES) {return ('' + CALORIES + ' \u043a\u0430\u043b.');}
function _UDS_MSG_PRICE_RANGE(RANGE) {return ('\u0414\u0438\u0430\u043f\u0430\u0437\u043e\u043d \u0446\u0435\u043d: ' + RANGE + '.');}
function _UDS_MSG_PRICE(PRICE) {return ('\u0426\u0435\u043d\u0430: ' + PRICE + '.');}
function _UDS_MSG_AVAILABILITY(AVAILABILITY) {return ('\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u043e\u0441\u0442\u044c: ' + AVAILABILITY + '.');}
function _UDS_MSG_TELEPHONE(TELEPHONE) {return ('\u0422\u0435\u043b.: ' + TELEPHONE);}
function _UDS_MSG_RESULT_INFO(NUMBER_OF_RESULTS, SEARCH_TIME) {return ('\u041d\u0430\u0439\u0434\u0435\u043d\u043e \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u043e\u0432: \u043f\u0440\u0438\u043c\u0435\u0440\u043d\u043e ' + NUMBER_OF_RESULTS + ' (\u0437\u0430 ' + SEARCH_TIME + ' \u0441\u0435\u043a.)');}
var _UDS_MSG_FILE_FORMAT = ('\u0424\u043e\u0440\u043c\u0430\u0442 \u0444\u0430\u0439\u043b\u0430:');
var _UDS_MSG_SHOWING_RESULTS_FOR = ('\u041f\u043e\u043a\u0430\u0437\u0430\u043d\u044b \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b \u043f\u043e \u0437\u0430\u043f\u0440\u043e\u0441\u0443');
var _UDS_MSG_SEARCH_INSTEAD_FOR = ('\u0418\u0441\u043a\u0430\u0442\u044c \u0432\u043c\u0435\u0441\u0442\u043e \u044d\u0442\u043e\u0433\u043e');
function _UDS_MSG_FILTERED_RESULTS(NUM) {return ('\u0427\u0442\u043e\u0431\u044b \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u043d\u0430\u0438\u0431\u043e\u043b\u0435\u0435 \u0437\u043d\u0430\u0447\u0438\u043c\u044b\u0435 \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b, \u043c\u044b \u043e\u043f\u0443\u0441\u0442\u0438\u043b\u0438 \u043d\u0435\u043a\u043e\u0442\u043e\u0440\u044b\u0435, \u043e\u0447\u0435\u043d\u044c \u043f\u043e\u0445\u043e\u0436\u0438\u0435 \u043d\u0430 ' + NUM + ' \u0443\u0436\u0435 \u043f\u043e\u043a\u0430\u0437\u0430\u043d\u043d\u044b\u0445. \u0415\u0441\u043b\u0438 \u0432\u044b \u0445\u043e\u0442\u0438\u0442\u0435, \u043c\u043e\u0436\u043d\u043e ' + '<a>\u043f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u044c \u043f\u043e\u0438\u0441\u043a, \u0432\u043a\u043b\u044e\u0447\u0438\u0432 \u043e\u043f\u0443\u0449\u0435\u043d\u043d\u044b\u0435 \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u044b' + '</a>.');}
var _UDS_MSG_ORDER_BY = ('\u0423\u043f\u043e\u0440\u044f\u0434\u043e\u0447\u0438\u0442\u044c:');
var _UDS_MSG_ORDER_BY_RELEVANCE = ('\u043f\u043e \u0440\u0435\u043b\u0435\u0432\u0430\u043d\u0442\u043d\u043e\u0441\u0442\u0438');
var _UDS_MSG_ORDER_BY_DATE = ('\u043f\u043e \u0434\u0430\u0442\u0435');

var b=!0,g=null,h=encodeURIComponent,l=google_exportSymbol,m=window,n=document,p=navigator,q="appendChild",r="push",s="status",t="createElement",u="ServiceBase",v="length",w="language",x="style",y="loader",z={blank:"&nbsp;"};z.image=_UDS_MSG_SEARCHER_IMAGE;z.web=_UDS_MSG_SEARCHER_WEB;z.blog=_UDS_MSG_SEARCHER_BLOG;z.video=_UDS_MSG_SEARCHER_VIDEO;z.local=_UDS_MSG_SEARCHER_LOCAL;z.news=_UDS_MSG_SEARCHER_NEWS;z.book=_UDS_MSG_SEARCHER_BOOK;z.patent=_UDS_MSG_SEARCHER_PATENT;z["ads-by-google"]=_UDS_MSG_ADS_BY_GOOGLE;
z.save=_UDS_MSG_SEARCHCONTROL_SAVE;z.keep=_UDS_MSG_SEARCHCONTROL_KEEP;z.include=_UDS_MSG_SEARCHCONTROL_INCLUDE;z.copy=_UDS_MSG_SEARCHCONTROL_COPY;z.close=_UDS_MSG_SEARCHCONTROL_CLOSE;z["sponsored-links"]=_UDS_MSG_SEARCHCONTROL_SPONSORED_LINKS;z["see-more"]=_UDS_MSG_SEARCHCONTROL_SEE_MORE;z.watermark=_UDS_MSG_SEARCHCONTROL_WATERMARK;z["search-location"]=_UDS_MSG_SEARCHER_CONFIG_SET_LOCATION;z["disable-address-lookup"]=_UDS_MSG_SEARCHER_CONFIG_DISABLE_ADDRESS_LOOKUP;z["sort-by-date"]=_UDS_MSG_SORT_BY_DATE;
z.pbg=_UDS_MSG_BRANDING_STRING;z["n-minutes-ago"]=_UDS_MSG_MINUTES_AGO;z["n-hours-ago"]=_UDS_MSG_HOURS_AGO;z["one-hour-ago"]=_UDS_MSG_ONE_HOUR_AGO;z["all-n-related"]=_UDS_MSG_NEWS_ALL_N_RELATED;z["related-articles"]=_UDS_MSG_NEWS_RELATED;z["page-count"]=_UDS_MSG_TOTAL_PAGE_COUNT;var A=[];A[0]=_UDS_MSG_MONTH_ABBR_JAN;A[1]=_UDS_MSG_MONTH_ABBR_FEB;A[2]=_UDS_MSG_MONTH_ABBR_MAR;A[3]=_UDS_MSG_MONTH_ABBR_APR;A[4]=_UDS_MSG_MONTH_ABBR_MAY;A[5]=_UDS_MSG_MONTH_ABBR_JUN;A[6]=_UDS_MSG_MONTH_ABBR_JUL;A[7]=_UDS_MSG_MONTH_ABBR_AUG;
A[8]=_UDS_MSG_MONTH_ABBR_SEP;A[9]=_UDS_MSG_MONTH_ABBR_OCT;A[10]=_UDS_MSG_MONTH_ABBR_NOV;A[11]=_UDS_MSG_MONTH_ABBR_DEC;z["month-abbr"]=A;z.directions=_UDS_MSG_DIRECTIONS;z["clear-results"]=_UDS_MSG_CLEAR_RESULTS;z["show-one-result"]=_UDS_MSG_SHOW_ONE_RESULT;z["show-more-results"]=_UDS_MSG_SHOW_MORE_RESULTS;z["show-all-results"]=_UDS_MSG_SHOW_ALL_RESULTS;z.settings=_UDS_MSG_SETTINGS;z.search=_UDS_MSG_SEARCH;z["search-uc"]=_UDS_MSG_SEARCH_UC;z["powered-by"]=_UDS_MSG_POWERED_BY;z.sa=_UDS_MSG_SEARCHER_GSA;
z.by=_UDS_MSG_SEARCHER_BY;z.code=_UDS_MSG_SEARCHER_CODE;z["unknown-license"]=_UDS_MSG_UNKNOWN_LICENSE;z["more-results"]=_UDS_MSG_SEARCHCONTROL_MORERESULTS;z.previous=_UDS_MSG_SEARCHCONTROL_PREVIOUS;z.next=_UDS_MSG_SEARCHCONTROL_NEXT;z["get-directions"]=_UDS_MSG_GET_DIRECTIONS;z["to-here"]=_UDS_MSG_GET_DIRECTIONS_TO_HERE;z["from-here"]=_UDS_MSG_GET_DIRECTIONS_FROM_HERE;z["clear-results-uc"]=_UDS_MSG_CLEAR_RESULTS_UC;z["search-the-map"]=_UDS_MSG_SEARCH_THE_MAP;z["scroll-results"]=_UDS_MSG_SCROLL_THROUGH_RESULTS;
z["edit-tags"]=_UDS_MSG_EDIT_TAGS;z["tag-search"]=_UDS_MSG_TAG_THIS_SEARCH;z["search-string"]=_UDS_MSG_SEARCH_STRING;z["optional-label"]=_UDS_MSG_OPTIONAL_LABEL;z["delete"]=_UDS_MSG_DELETE;z.deleted=_UDS_MSG_DELETED;z.cancel=_UDS_MSG_CANCEL;z["upload-video"]=_UDS_MSG_UPLOAD_YOUR_VIDEOS;z["im-done"]=_UDS_MSG_IM_DONE_WATCHING;z["close-player"]=_UDS_MSG_CLOSE_VIDEO_PLAYER;z["no-results"]=_UDS_MSG_NO_RESULTS;z["linked-cse-error-results"]=_UDS_MSG_LINKEDCSE_ERROR_RESULTS;z.back=_UDS_MSG_BACK;
z.subscribe=_UDS_MSG_SUBSCRIBE;z["us-pat"]=_UDS_MSG_USPAT;z["us-pat-app"]=_UDS_MSG_USPAT_APP;z["us-pat-filed"]=_UDS_MSG_PATENT_FILED;z.dym=_UDS_MSG_DID_YOU_MEAN;z["showing-results-for"]=_UDS_MSG_SHOWING_RESULTS_FOR;z["search-instead-for"]=_UDS_MSG_SEARCH_INSTEAD_FOR;z["custom-search"]=_UDS_MSG_CUSTOM_SEARCH;z.labeled=_UDS_MSG_LABELED;z.loading=_UDS_MSG_LOADING;z["all-results-short"]=_UDS_MSG_ALL_RESULTS_SHORT;z["all-results-long"]=_UDS_MSG_ALL_RESULTS_LONG;z["refine-results"]=_UDS_MSG_REFINE_RESULTS;
z["result-info"]=_UDS_MSG_RESULT_INFO;z["file-format"]=_UDS_MSG_FILE_FORMAT;z["order-results-by"]="Sort by:";z["order-by-relevance"]="Relevance";z["order-by-date"]="Date";var _json_cache_defeater_=(new Date).getTime(),_json_request_require_prep=b;function B(a,d){C("msie")&&("msie 6.0"in D?D["msie 6.0"]:D["msie 6.0"]=-1!=p.appVersion.toLowerCase().indexOf("msie 6.0"))?m.setTimeout(E(this,F,[a,d]),0):F(a,d)}
function F(a,d){var c=n.getElementsByTagName("head")[0];c||(c=n.body.parentNode[q](n[t]("head")));var e=n[t]("script");e.type="text/javascript";e.charset="utf-8";var f=_json_request_require_prep?a+"&key="+google[y].ApiKey+"&v="+d:a;if(C("msie")||C("safari")||C("konqueror"))f=f+"&nocache="+_json_cache_defeater_++;e.src=f;var k=function(){e.onload=g;e.parentNode.removeChild(e)},f=function(a){a=(a?a:m.event).target?(a?a:m.event).target:(a?a:m.event).srcElement;if("loaded"==a.readyState||"complete"==
a.readyState)a.onreadystatechange=g,k()};"Gecko"==p.product?e.onload=k:e.onreadystatechange=f;c[q](e)}function E(a,d,c){return function(){return d.apply(a,c||[])}}function C(a){return a in G?G[a]:G[a]=-1!=p.userAgent.toLowerCase().indexOf(a)}var G={},D={},H,I;m.ActiveXObject&&(H=b,m.XMLHttpRequest&&(I=b));if(!J)var J=l;if(!K)var K=google_exportProperty;
google[w].d={AFRIKAANS:"af",ALBANIAN:"sq",AMHARIC:"am",ARABIC:"ar",ARMENIAN:"hy",AZERBAIJANI:"az",BASQUE:"eu",BELARUSIAN:"be",BENGALI:"bn",BIHARI:"bh",BULGARIAN:"bg",BURMESE:"my",BRETON:"br",CATALAN:"ca",CHEROKEE:"chr",CHINESE:"zh",CHINESE_SIMPLIFIED:"zh-CN",CHINESE_TRADITIONAL:"zh-TW",CORSICAN:"co",CROATIAN:"hr",CZECH:"cs",DANISH:"da",DHIVEHI:"dv",DUTCH:"nl",ENGLISH:"en",ESPERANTO:"eo",ESTONIAN:"et",FAROESE:"fo",FILIPINO:"tl",FINNISH:"fi",FRENCH:"fr",FRISIAN:"fy",GALICIAN:"gl",GEORGIAN:"ka",GERMAN:"de",
GREEK:"el",GUJARATI:"gu",HAITIAN_CREOLE:"ht",HEBREW:"iw",HINDI:"hi",HUNGARIAN:"hu",ICELANDIC:"is",INDONESIAN:"id",INUKTITUT:"iu",IRISH:"ga",ITALIAN:"it",JAPANESE:"ja",JAVANESE:"jw",KANNADA:"kn",KAZAKH:"kk",KHMER:"km",KOREAN:"ko",KURDISH:"ku",KYRGYZ:"ky",LAO:"lo",LAOTHIAN:"lo",LATIN:"la",LATVIAN:"lv",LITHUANIAN:"lt",LUXEMBOURGISH:"lb",MACEDONIAN:"mk",MALAY:"ms",MALAYALAM:"ml",MALTESE:"mt",MAORI:"mi",MARATHI:"mr",MONGOLIAN:"mn",NEPALI:"ne",NORWEGIAN:"no",OCCITAN:"oc",ORIYA:"or",PASHTO:"ps",PERSIAN:"fa",
POLISH:"pl",PORTUGUESE:"pt",PORTUGUESE_PORTUGAL:"pt-PT",PUNJABI:"pa",QUECHUA:"qu",ROMANIAN:"ro",RUSSIAN:"ru",SANSKRIT:"sa",SCOTS_GAELIC:"gd",SERBIAN:"sr",SINDHI:"sd",SINHALESE:"si",SLOVAK:"sk",SLOVENIAN:"sl",SPANISH:"es",SUNDANESE:"su",SWAHILI:"sw",SWEDISH:"sv",SYRIAC:"syr",TAJIK:"tg",TAMIL:"ta",TAGALOG:"tl",TATAR:"tt",TELUGU:"te",THAI:"th",TIBETAN:"bo",TONGA:"to",TURKISH:"tr",UKRAINIAN:"uk",URDU:"ur",UZBEK:"uz",UIGHUR:"ug",VIETNAMESE:"vi",WELSH:"cy",YIDDISH:"yi",YORUBA:"yo",UNKNOWN:""};
J("google.language.Languages",google[w].d);
var L={AMHARIC:"am",ARMENIAN:"hy",AZERBAIJANI:"az",BASQUE:"eu",BENGALI:"bn",BIHARI:"bh",BRETON:"br",BURMESE:"my",CHEROKEE:"chr",CORSICAN:"co",DHIVEHI:"dv",ESPERANTO:"eo",FAROESE:"fo",FRISIAN:"fy",GEORGIAN:"ka",GUJARATI:"gu",INUKTITUT:"iu",JAVANESE:"jw",KANNADA:"kn",KAZAKH:"kk",KHMER:"km",KURDISH:"ku",KYRGYZ:"ky",LAO:"lo",LAOTHIAN:"lo",LATIN:"la",LUXEMBOURGISH:"lb",MALAYALAM:"ml",MAORI:"mi",MARATHI:"mr",MONGOLIAN:"mn",NEPALI:"ne",OCCITAN:"oc",ORIYA:"or",PASHTO:"ps",PUNJABI:"pa",QUECHUA:"qu",SANSKRIT:"sa",
SCOTS_GAELIC:"gd",SINDHI:"sd",SINHALESE:"si",SUNDANESE:"su",SYRIAC:"syr",TAJIK:"tg",TAMIL:"ta",TATAR:"tt",TELUGU:"te",TIBETAN:"bo",TONGA:"to",UIGHUR:"ug",URDU:"ur",UZBEK:"uz",YORUBA:"yo"},M={},N={},O=100;function P(a,d){var c="id"+O++;N[c]=function(e){e=d(e);a(e);delete N[c]};return"google.language.callbacks."+c}function Q(a,d){var c="id"+O++;N[c]=function(e,f,k,U,X){e=d(e,f,k,U,X);a(e);delete N[c]};return"google.language.callbacks."+c}google[w].k=function(a){return M[a]};
J("google.language.isTranslatable",google[w].k);for(var R in google[w].d)M[google[w].d[R]]=b;for(R in L)M[L[R]]=!1;l("google.language.callbacks",N);
l("google.language.getBranding",function(a,d){var c="horizontal";d&&d.type&&(c=d.orientation);var e=z["powered-by"],f=google[y][u]+"/css/small-logo"+(H&&!I?".gif":".png"),k=['<div style="font-family:arial,sans-serif;','font-size:11px;margin-bottom:1px" class="gBrandingText">',e,'</div><div><img src="',f,'"/></div>'],e=['<span style="vertical-align:middle;font-family:arial,sans-serif;','font-size:11px;" class="gBrandingText">',e,'<img style="padding-left:1px;vertical-align:',H?'bottom;" ':'middle;" ',
'src="',f,'"/></span>'],e="horizontal"==c?e:k,f=e.join(""),c=n[t]("div");c.className="gBranding";c[x].color="#676767";e==k&&(c[x].textAlign="center");c.innerHTML=f;a&&(k=g,(k="string"==typeof a?n.getElementById(a):a)&&k[q]&&k[q](c));return c});l("google.language.HORIZONTAL_BRANDING","horizontal");l("google.language.VERTICAL_BRANDING","vertical");l("google.language.CurrentLocale",_UDS_CONST_LOCALE);l("google.language.ShortDatePattern",_UDS_CONST_SHORT_DATE_PATTERN);google[w].l=function(a,d,c){d=P(d,S);d="http://www.google.com/complete/search"+("?json=t&jsonp="+d+"&client=uds");c&&(d+="&hl="+h(c));d+="&q="+h(a);_json_request_require_prep=!1;B(d,g);_json_request_require_prep=b};J("google.language.suggest",google[w].l);function S(a){var d={};d.query=a[0];d.suggestions=[];var c=a[1];a=a[2];for(var e=0;e<c[v];e++){var f={};f.name=c[e];f.count=parseInt(a[e].replace(",",""),10);f.results=a[e];d.suggestions[r](f)}return d};google[w].f={TEXT:"text",HTML:"html"};J("google.language.ContentType",google[w].f);google[w].translate=function(a,d,c,e){var f,k=g;if("string"==typeof a)f=a;else if(a.text&&a.type)f=a.text,k=a.type;else throw"Invalid first argument";5120>=f[v]?a=!1:(a=T(g,g,400,"the string to be translated exceeds the maximum length allowed",g),e(a),a=b);a||(e=Q(e,T),e=google[y][u]+"/Gtranslate?callback="+e,e=e+"&context=22"+("&q="+h(f)),e+="&langpair="+h(d+"|"+c),k&&(e+="&format="+h(k)),B(e,google[w].Version))};
J("google.language.translate",google[w].translate);function T(a,d,c,e){a={};a.status={code:c};e&&(a[s].message=e);200!=c?(a.error=a[s],a.translation=""):(a.translation=d.translatedText,d.detectedSourceLanguage&&(a.detectedSourceLanguage=d.detectedSourceLanguage));return a}google[w].i=function(a,d){var c=Q(d,V),c=google[y][u]+"/GlangDetect?callback="+c,c=c+"&context=22"+("&q="+h(a));B(c,google[w].Version)};J("google.language.detect",google[w].i);
function V(a,d,c,e){a={};a.status={code:c};e&&(a[s].message=e);200!=c?(a.error=a[s],a.language=""):(a.language=d[w],a.isReliable=d.isReliable,a.confidence=d.confidence);return a};var W={"en|am":b,"en|ar":b,"en|bn":b,"en|el":b,"en|fa":b,"en|gu":b,"en|hi":b,"en|kn":b,"en|ml":b,"en|mr":b,"en|ne":b,"en|or":b,"en|pa":b,"en|ru":b,"en|sa":b,"en|si":b,"en|sr":b,"en|ta":b,"en|te":b,"en|ti":b,"en|ur":b,"en|zh":b};google[w].m=function(a,d,c,e){if("function"!=typeof e)throw"Invalid callback";if(aa(a,d,c,e)){e=Q(e,Y);d=[google[y][u],"/Gtransliterate?callback=",e,"&context=22","&langpair=",h(d+"|"+c)];for(c=0;c<a[v];c++)d[r]("&q="),d[r](h(a[c]));B(d.join(""),google[w].Version)}};
J("google.language.transliterate",google[w].m);
function aa(a,d,c,e){var f="";"object"!=typeof a||!a[v]?f="Words needs to be an array.":1>a[v]?f="No words to transliterate.":5<a[v]?f="Number of words to transliterate exceeds the limit of 5":d?c?d&&c&&W[d+"|"+c]||(f="Transliteration not supported for the language pair. Source Language - "+d+" Destination Language - "+c+"."):f="Destination language not specified.":f="Source language not specified.";if(0<f[v]){var k=Y(g,g,400,f,g);m.setTimeout(function(){e(k)},0);return!1}return b}
function Y(a,d,c,e){a={status:{code:c,message:e}};200!=c?(a.error=a[s],a.transliterations=[]):a.transliterations=d.transliterations;return a};var ba={hi:b,kn:b,ml:b,ta:b,te:b};google[w].c={h:0,g:1,e:2};google[w].j=function(a){a=a.toLowerCase();return a in ba?ca(a):google[w].c.e};J("google.language.FontRenderingStatus.SUPPORTED",google[w].c.g);J("google.language.FontRenderingStatus.UNSUPPORTED",google[w].c.h);J("google.language.FontRenderingStatus.UNKNOWN",google[w].c.e);J("google.language.isFontRenderingSupported",google[w].j);
function ca(a){switch(a){case "ml":return a=[],a[r]({a:"\u0d23\u0d28\u0d4d\u200d",b:"\u0d23\u0d4d\u0d23\u0d28\u0d4d\u0d31"}),a[r]({a:"\u0d23\u0d28\u0d4d\u200d",b:"\u0d23\u0d4d\u0d23\u0d28\u0d4d\u200d\u0d31"}),Z(a);case "hi":return Z([{a:"\u0915\u094d\u0930\u0930\u094d\u0925",b:"\u0915\u0925"}]);case "kn":return Z([{a:"\u0c95\u0ccd\u0cb2",b:"\u0c95"}]);case "te":return Z([{a:"\u0c15\u0c4d\u0c32",b:"\u0c15"}]);case "ta":return Z([{a:"\u0b95\u0bcd",b:"\u0b95"}])}}
function Z(a){for(var d=0;d<a[v];d++){var c=a[d],e=c.a,f=c.b,c=n[t]("span");c[x].fontSize="10px";var k=c[x];"opacity"in k?k.opacity=0.1:"MozOpacity"in k?k.MozOpacity=0.1:"filter"in k&&(k.filter="alpha(opacity=10)");n.body[q](c);c.innerHTML=e;e=$(c).width;c.innerHTML=f;f=$(c).width;n.body.removeChild(c);if(e==f)return b}return!1}
function $(a){if("none"!=a[x].display)return{width:a.offsetWidth,height:a.offsetHeight};var d=a[x],c=d.display,e=d.visibility,f=d.position;d.visibility="hidden";d.position="absolute";d.display="";var k=a.offsetWidth;a=a.offsetHeight;d.display=c;d.position=f;d.visibility=e;return{width:k,height:a}};
google.loader.loaded({"module":"language","version":"1.0","components":["default"]});
google.loader.eval.language = function() {eval(arguments[0]);};if (google.loader.eval.scripts && google.loader.eval.scripts['language']) {(function() {var scripts = google.loader.eval.scripts['language'];for (var i = 0; i < scripts.length; i++) {google.loader.eval.language(scripts[i]);}})();google.loader.eval.scripts['language'] = null;}})();