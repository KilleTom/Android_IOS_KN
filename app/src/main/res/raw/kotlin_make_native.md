[TOC]



# Kotlin 实现跨平台网络请求

## Kotlin 与 Flutter 跨平台的一个小的对比

`Kotlin` 跨平台的目标：为更多的平台提供一套可以公共管理的代码，相对较少的牵涉各个平台的UI，其目的更多的在乎使用同一套通用的业务逻辑代码，为你的多平台项目提供一套逻辑解决方案从而不牵涉平台的UI。

`Flutter` 跨平台的目标：利用一套新的UI框架，一套新的渲染机制去更好的兼容多个平台。而且Flutter更是谷歌新一代的操作系统`Fuchsia`的先驱者。目前`Flutter`生态较好，有较多的开源框架或者开源人员去支撑它的发展，`Google`更是为了`Flutter`时常举行一些比赛或者发一些推文，推行`Flutter`的发展。



## 什么是ktor 
Ktor 是一个使用强大的 Kotlin 语言在互联系统中构建异步服务器与客户端的框架。

## 什么是Kotr-client

Kotr-client 是基于ktor理念，开发出的一套跨平台客户端网络框架。

利用 Kotr-client 实现对多平台客户端网络请求的统一的代码管理。针对公司业务的通用api、或当前产品中通用的业务api，利用模块化、组件化思维，以及一些设计思想，则可以封装出对多端平台统一的网络请求库。

## Ktor-client的使用

接下来的讲解都会以Android 、IOS利用ktor实现一套代码管理两端客户的网络请求

### 创建Mobile工程

首先打开IDEA选择创建Moblie类型项目如下图

![](.\chose_project_type.png)

然后选择Java对应版本

![](.\chose_jvm_version.png)

最后输入项目名称构建项目

![](.\iput_project_name.png)

### 工程相关配置讲解

针对项目中`app\build.gradle`配置文件进行一些常规讲解

```groovy
//项目公用配置 例如远程仓库来源 公用plugin等
plugins {
    id 'org.jetbrains.kotlin.multiplatform' version '1.3.72'
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.3.70'

}
repositories {
    mavenCentral()
    jcenter()
    google()
}
apply plugin: 'com.android.application'
apply plugin: 'kotlin-android-extensions'
apply plugin: 'kotlinx-serialization'

//android 版本编译配置
android {
//此处省略部分代码
}
//android 远程库依赖配置
dependencies {
  //此处省略部分代码
}

kotlin {
//    android 平台别名
    android("android")
// IOS平台别名
    iosX64("ios") {
        binaries {
            framework()
        }
    }
//    跨平台依赖设置
    sourceSets {
//        公共库配置 也就是跨平台公用的代码库 可用于构建base模块
        commonMain {
            dependencies {
           //此处省略部分代码
        }}
//        公共库测试模块
        commonTest {
            dependencies {
    //此处省略部分代码
        }
//        android 模块
        androidMain {
            dependencies {
     //此处省略部分代码
        }
//        android test 配置
        androidTest {
            dependencies {
                implementation kotlin('test')
                implementation kotlin('test-junit')
            }
        }
//        IOS 模块
        iosMain {
            dependencies {
              //此处省略部分代码
            }
        }
//        IOS test 配置
        iosTest {
        }
    }
}
}
}
// IOS native 的一些配置
task copyFramework {
    def buildType = project.findProperty('kotlin.build.type') ?: 'DEBUG'
    def target = project.findProperty('kotlin.target') ?: 'ios'
    dependsOn kotlin.targets."$target".binaries.getFramework(buildType).linkTask

    doLast {
        def srcFile = kotlin.targets."$target".binaries.getFramework(buildType).outputFile
        def targetDir = getProperty('configuration.build.dir')
        copy {
            from srcFile.parent
            into targetDir
            include 'app.framework/**'
            include 'app.framework.dSYM'
        }
    }
}
```

注意Common的配置是必须的，你可以这样理解，跨平台的多端代码实现思想都是基于Common的，也就是说把Common当做一个有具体行为但是部分细节是抽象化的一个对象，而其他平台则是基于对Common这个对象针对平台化的技术细节实现。

### ktor-client的配置

在`app\build.gradle`下对`sourceSets`一些模块进行配置

```groovy
 sourceSets {
//        公共库配置 也就是跨平台公用的代码库 可用于构建base模块
        commonMain {
            dependencies {
                implementation kotlin('stdlib-common')

                // 设置支持ktor-client
      			implementation("io.ktor:ktor-client-core:$ktor_version")
      			// 设置相关的json配置
      			implementation("io.ktor:ktor-client-json:$ktor_version")
      			implementation("io.ktor:ktor-client-serialization:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-io:$kotlinx_io_version")

                implementation 
                "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serialization_version"
                
                implementation "org.jetbrains.kotlin:kotlin-reflect:1.3.72"
            }
        }
//        公共库测试模块
        commonTest {
            dependencies {
                implementation kotlin('test-common')
                implementation kotlin('test-annotations-common')
//                implementation("org.jetbrains.kotlinx:kotlinx-io:$kotlinx_io_version")
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-json:$ktor_version")
                implementation("io.ktor:ktor-client-serialization:$ktor_version")
                implementation "org.jetbrains.kotlin:kotlin-reflect:1.3.72"
            }
        }
//        android 模块
        androidMain {
            dependencies {
                implementation kotlin('stdlib')

                implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:$kotlinx_io_version")

                implementation("io.ktor:ktor-client-android:$ktor_version")
                implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
                implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")
                implementation "org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serialization_version"
            }
        }
//        android test 配置
        androidTest {
            dependencies {
                implementation kotlin('test')
                implementation kotlin('test-junit')
            }
        }
//        IOS 模块
        iosMain {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktor_version")
                implementation("io.ktor:ktor-client-json-native:$ktor_version")
                implementation("io.ktor:ktor-client-serialization-native:$ktor_version")
                implementation "org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serialization_version"

                implementation("org.jetbrains.kotlinx:kotlinx-io-native:$kotlinx_io_version")
            }
        }
//        IOS test 配置
        iosTest {
        }
    }
```

注意：

对应平台模块也必须依赖相关配置，避免平台模块调用common模块代码时无法调用
本示例代码使用的序列化是用`Kotlinx.Serialization`,目前还支持`Gson`、`Jackson`相应配置请参考：
`https://ktor.kotlincn.net/clients/http-client/features/json-feature.html`



### ktor-client的简单使用

接下来以Android为示例代码进行讲解（本示例代码以请求聚合数据新闻API为例子进行讲解）

```kotlin
fun netTest(){
    //初始化httpClient
    val client = HttpClient() {
        //这里做请求网络的一些属性配置例如序列化、拦截器等
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
    //然后开始请求网络
    val response = client.post<NewsResponse>(HttpRequestBuilder().apply {
            url.host = "v.juhe.cn"
            url.protocol = URLProtocol.HTTPS
            url.encodedPath = "/toutiao/index"
            url.parameters.append("type", type)
            url.parameters.append("key", newsApiKey)

        })
    
    if (response.reason == "成功的返回") {

            val result = response.newsResult 
        ?: throw ApiResultException("news result null")
            val datas = result.data

            return datas
        }

        throw RunTimeException(response.reason ?: "unknown error")
}
```

//网络对应数据bean如下

```kotlin
@Serializable
data class NewsResponse(
    @SerialName("error_code")
    var errorCode: Int,
    @SerialName("reason")
    var reason: String?,
    @SerialName("result")
    var newsResult: NewsResult?
)
@Serializable
data class NewsData(

    @SerialName("author_name")
    var authorName: String?,
    @SerialName("category")
    var category: String?,

    @SerialName("date")
    var date: String = "",

    @SerialName("thumbnail_pic_s")
    var thumbnailPicS: String = "",
    @SerialName("thumbnail_pic_s02")
    var thumbnailPicS02: String = "",
    @SerialName("thumbnail_pic_s03")
    var thumbnailPicS03: String = "",
    @SerialName("title")
    var title: String?,
    @SerialName("uniquekey")
    var uniquekey: String?,
    @SerialName("url")
    var url: String?
)
```

### ktor-client的简易封装以及调用

使用kror作为跨平台网络请求，那么对于一些通用的的网络业务API其实可以封装到一个属于该项目的base Net lib。

在进行代码示例演示前将讲解下跨平台相关的两个关键字`expect`、`actual`
expect：标记为平台必须实现的对象，而在公用模块也就是`common`模块是expect标记的对象只是一个抽象对象，具体要有相应平台去实现
actual：对应平台的下将`expect`标识的对象实例化相应平台所应该使用的对象
如下：
```kotlin
//在Common平台声明这个对象是CoroutineDispatcher 但并没有实例化 是一个抽象对象
internal expect val APIDispatcher: CoroutineDispatcher
//在Android 平台下 实例化该对象
internal actual val APIDispatcher: CoroutineDispatcher = Dispatchers.Default
```
```kotlin
//利用Manager的思想封装一个对HttpClient统一管理的类
class NetClientManager private constructor() {


    val client: HttpClient

    init {

        client = HttpClient() {

            install(JsonFeature) {
                serializer = KotlinxSerializer()

            }
        }
    }

    companion object {
        val instarnce by lazy { NetClientManager() }
    }
}

//其次针对通用API的请求可以这样实现
internal expect val APIDispatcher: CoroutineDispatcher

class Api private constructor() {

    private var newsApiKey = "聚合数据的ApiKey"

    //这里使用协程做请求
    private suspend fun getNews(type: String = "top"): List<NewsData> {

        val clientManager = NetClientManager.instarnce
        
        val client = clientManager.client

        val response = client.post<NewsResponse>(HttpRequestBuilder()
        .apply {

            url.host = "v.juhe.cn"
            url.protocol = URLProtocol.HTTPS
            url.encodedPath = "/toutiao/index"
            url.parameters.append("type", type)
            url.parameters.append("key", newsApiKey)

        })

        if (response.reason == "成功的返回") {

            val result = response.newsResult ?: throw ApiResultException("news result null")
            val datas = result.data

            return datas
        }

        throw ApiResultException(response.reason ?: "unknown error")
    }

    fun getNewsResult(
        type: String = "",
        dataAction: (List<NewsData>) -> Unit,
        errorAction: (Exception) -> Unit,
        dispatcher: CoroutineDispatcher = APIDispatcher
    ) {
        GlobalScope.apply {

            launch(dispatcher) {

                try {

                    val result = getNews(type)
                    dataAction.invoke(result)

                } catch (e: Exception) {

                    errorAction.invoke(e)
                }
            }
        }
    }

    companion object {

        val instance by lazy { Api() }
    }

}
```



Android的调用示例如下

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sample().checkMe()
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.main_text).text = hello()
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.main_text).text = hello()

        main_text.setOnClickListener {

            Api.instance.getNewsResult(dataAction = {
                Log.i("KilleTom-Ypz", "$it")
                if (!it.isNullOrEmpty()){
                    post
                }
            }, errorAction = {
                it.printStackTrace()
            })
        }
    }
}
```
启动App效果如下:
![](.\android_frist_run.png)
点击Hello Android获取网络数据效果如下:
![](.\android_request_net_result.png)
## ktor-client的总结
使用kotr-client 需要注意在公共模块下需要配置好基础依赖，其次在各个平台下配置好对应以实现好的库进行配置依赖。

其次需要注意如下几点:

1. 将`HttpClient`初始化为对应平台的`HttpClient`
2. 实现好对应平台的协程的`CoroutineDispatcher` 也就是 `expect`、`actual`掌握以及运用
3. 利用抽象概念统一封装好对公用API网络请求以及结果回调
4. 简单调用封装好的网络请求实现即可