package moscow.rockstar;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectTest {
    public static void main(String[] args) {
        try {
            Class<?> blendFunctionClass = Class.forName("com.mojang.blaze3d.pipeline.BlendFunction");
            for (Field f : blendFunctionClass.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == blendFunctionClass) {
                    f.setAccessible(true);
                    Object func = f.get(null);
                    System.out.println("BlendFunction." + f.getName() + ":");
                    for (Field subF : blendFunctionClass.getDeclaredFields()) {
                        subF.setAccessible(true);
                        if (!Modifier.isStatic(subF.getModifiers())) {
                            System.out.println("  " + subF.getName() + " = " + subF.get(func));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
