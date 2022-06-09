# AnnotationLoader
AnnotationLoader is a library intended to bring runtime class loading with ease.
The idea came about when creating Discord bots, and I kept forgetting to add new
commands to a sort of command handler class, leading to additional builds of the
bot.

# Getting Started
## <ins>Installation</ins>
### Gradle
```
repositories {
    maven {
        name 'jitpack'
        url 'https://jitpack.io'
    }
}
dependencies {
    implementation 'com.github.TinyPandas:AnnotationLoader:master-013cba967c-1'
}
```

### Maven
```
<repositories>
    <repository>
	<id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependency>
    <groupId>com.github.TinyPandas</groupId>
    <artifactId>AnnotationLoader</artifactId>
    <version>1.0</version>
</dependency>
```

## Implementation
### Requirement
For `AnnotationLoader` to work correctly, your main class must extend it.

```java
public class MainClass extends AnnotationLoader {
    public static void main(String[] args) {
        System.out.println("Loaded");
    }
}
```

### Annotations
In the current state, `@RegisterOnStart` will only register other `Annotation`s.

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.tinypandas.annotations.RegisterOnStart;

@Retention(RetentionPolicy.RUNTIME)
@RegisterOnStart
public @interface Command {
    public String name() default: "";
    public String desc() default: "";
}
```

### Loaders
`AnnotationLoader` comes out of the box with a `DefaultLoader`. This will work
for classes that have **zero-arg** constructors. However, if you need to create
classes that have arg based constructors, you can define your own loader.

To use the `DefaultLoader` add `@LoaderInfo` to your class.

Using a custom loader:
```java
// Used to tell AnnotationLoader that this class is a loader.
@LoaderInfo(isLoader = true)
public class LoaderTest extends Loader {

    @Override
    public <T> T registerClass(Class<T> clazz) {
        System.out.println("Registering: " + clazz);
        try {
            System.out.println("Registered: " + clazz);
            // Gets a constructor requiring two Strings, 
            // then passes "test" and "Hello World!" to it.
            // You can also get Annotations from the clazz
            // by doing clazz.getAnnotation(Command.class)
            // and extract data from it that way to pass
            // to the constructor.
            return clazz.getDeclaredConstructor(String.class, String.class)
                    .newInstance("test", "Hello World!");
        } catch (Exception e) {
            System.out.println("Failed: " + clazz);
            e.printStackTrace();
            return null;
        }
    }
}
```

### Class Usage

```java
// Annotate the class with our RegisterOnStart Annotation
@Command(name = "test", desc = "This is some command description.")
// Annotate the class with our LoaderInfo
@LoaderInfo(loader = LoaderTest.class)
public class TestCommand extends Command {
    
    private final String name;
    private final String description;
    
    public TestCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
```