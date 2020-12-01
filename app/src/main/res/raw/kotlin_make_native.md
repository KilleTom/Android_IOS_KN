[TOC]



# Kotlin 实现跨平台网络请求

## 什么是ktor 
Ktor 是一个使用强大的 Kotlin 语言在互联系统中构建异步服务器与客户端的框架。

## 什么是Kotr-client

Kotr-client 是基于ktor理念，开发出的一套跨平台客户端网络框架。

利用 Kotr-client 实现对多平台客户端网络请求的统一的代码管理。针对公司业务的通用api、或当前产品中通用的业务api，利用模块化、组件化思维，以及一些设计思想，则可以封装出对多端平台统一的网络请求库。

## Ktor-client的使用

接下来的讲解都会以Android 、IOS利用ktor实现一套代码管理两端客户的网络请求

### 创建跨平台工程

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
        }
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
     
     //点击请求网络
        main_text.setOnClickListener {
            Api.instance.getNewsResult(dataAction = {
                Log.i("KilleTom-Ypz", it.toString())
            }, errorAction = {
                it.printStackTrace()
            })
        }
    }
}
//运行结果如下：
//I/KilleTom-Ypz: 
// NewsData(
// authorName=知心体育, 
// category=头条, 
// date=2020-12-02 01:00, 
// thumbnailPicS=https://00imgmini.eastday.com/mobile/20201202/20201202010022_bf5a57828edeab26dbca7440cdfa75db_1_mwpm_03200403.jpg, thumbnailPicS02=http://00imgmini.eastday.com/mobile/20201202/20201202010022_bf5a57828edeab26dbca7440cdfa75db_5_mwpm_03200403.jpg, thumbnailPicS03=http://00imgmini.eastday.com/mobile/20201202/20201202010022_bf5a57828edeab26dbca7440cdfa75db_2_mwpm_03200403.jpg, title=比广州恒大还丢人！上港队员踢球态度遭炮轰，这种球员不能进国家队, uniquekey=90a3dbeb77496daeae512e41af409d26, 
// url=https://mini.eastday.com/mobile/201202010022757.html)
```

## ktor-client的总结
使用kotr-client 需要注意在公共模块下需要配置好基础依赖，其次在各个平台下配置好对应以实现好的库进行配置依赖。

其次需要注意如下几点:

1. 将`HttpClient`初始化为对应平台的`HttpClient`
2. 实现好对应平台的协程的`CoroutineDispatcher`
3. 利用抽象概念统一封装好对公用API网络请求以及结果回调
4. 简单调用封装好的网络请求实现即可