
/**
 * 一些通用的基本的工具方法
 */
    public class JavaUtils {

    static JavaObjectSerializer javaObjectSerializer = new JavaObjectSerializer();

    /**
     * 深度克隆对象
     */
    public static <T> T clone(T value) {
        if (value == null) {
            return null;
        }

        byte[] bytes = javaObjectSerializer.toBytes(value);
        T value2 = javaObjectSerializer.toObject(bytes, null);
        return value2;
    }

    /**
     * 切分一个大list成许多个小list
     */
    public static <T> List<List<T>> splitList(List<T> largeList, int maxItemCount) {
        List<List<T>> subLists = new ArrayList<>();
        if (largeList == null) {
            return subLists;
        }

        // 计算子列表的数量
        int listCount = largeList.size() / maxItemCount;
        if (largeList.size() % maxItemCount > 0) {
            ++listCount;
        }

        // 切割列表
        for (int i = 0; i < listCount; ++i) {
            int startIndex = i * maxItemCount;
            int endIndex = (i + 1) * maxItemCount;
            if (endIndex > largeList.size()) {
                endIndex = largeList.size();
            }

            List<T> subList = largeList.subList(startIndex, endIndex);
            subLists.add(subList);
        }

        return subLists;
    }

    /**
     * int(分) 转BigDecimal(元)
     */
    public static BigDecimal convertCentToYuan(int cent) {
        BigDecimal decimal = new BigDecimal(cent);
        decimal = decimal.divide(new BigDecimal(100));
        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * BigDecimal(元)转 int(分)
     */
    public static int convertYuanToCent(BigDecimal yuan) {
        BigDecimal decimal = yuan.setScale(2, BigDecimal.ROUND_HALF_UP);
        return decimal.multiply(new BigDecimal(100)).intValue();
    }

    /**
     * int(分)转 String(元)
     */
    public static String convertCentToYuan2(int cent) {
      return convertCentToYuan(cent).toString();
    }

    /**
     * String(元)转 int(分)
     */
    public static int convertYuanToCent2(String yuan) {
      return convertYuanToCent(new BigDecimal(yuan));
    }

    public static String toIdString(int[] ids) {
        return toIdString(Ints.asList(ids));
    }

    public static String toIdString(long[] ids) {
        return toIdString(Longs.asList(ids));
    }

    public static String toIdString(List ids) {
        if (ids == null || ids.size() <= 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.valueOf(ids.get(0)));
        for (int index = 1; index < ids.size(); ++index) {
            builder.append(",");
            builder.append(String.valueOf(ids.get(index)));
        }
        return builder.toString();
    }

    /**
     * List去除重复元素 支持调用方自定义元素重复规则
     */
    public static <T> List<T> distinct(List<T> source , EqualsFunc<? super  T> func) {
        List<T> data = Lists.newArrayList();
        if(CollectionUtils.isEmpty(source) || Objects.isNull(func)) {
            return data;
        }
        for(T t : source) {
            data.add(t);
        }
        for(int i = 0 ; i < data.size() - 1 ; i++) {
            for(int j = data.size() - 1 ; j > i ; j--) {
                if(func.invoke(data.get(i) , data.get(j))) {
                    data.remove(j);
                }
            }
        }
        return data;
    }

    /**
     * 对目标List进行条件过滤  支持调用方自定义过滤规则
     */
    public static <T> List<T> filter(List<? extends T> originList, FilterFunc<T> function) {
        List<T> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(originList) || Objects.isNull(function)) {
            return resultList;
        }
        for (T t : originList) {
            if (function.filter(t)) {
                resultList.add(t);
            }
        }
        return resultList;
    }

    /**
     * 读取整个文件为字符串
     * @param path 文件路径
     * @return 文件内容
     */
    public static String readStringFromFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
