package com.InventoryManagement;

/**
 * Basic class for getting the different names of a class
 * Probably the most unneeded class
 * Why did I implement? Why not
 * */
public class Names {
    /**
     * Get lowercase name of a class
     * @param cls The class whose name to return
     * @return The name of the class
     */
    public static String getLowerName(Class<?> cls) {
        return getSimpleName(cls).toLowerCase();
    }


    /**
     * Get the name of the class as it is
     * @param cls The class whose name to return
     * @return Name of the class
     */
    public static String getSimpleName(Class<?> cls) {
        String[] names = cls.getName().split("\\.");
        return names[names.length-1];
    }

    public static String getPluralName(Class<?> cls) {
        try {
            return cls.getField("plural").toString();
        } catch (NoSuchFieldException e) {
            return getLowerName(cls) + "s";
        }
    }
}
