# DiffusionAnim
椭圆渐变动画，一个模仿Android 12+的启动页动画，适用更多的界面，使用简单。

# 效果图

![PhotoView](./demo/demo.gif)

# 注意事项

暂无

# 使用
1.在 settings.gradle 里添加它：

```gradle
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
  }
}
```
2.添加依赖：

```gradle
dependencies {
  implementation 'com.github.unmiHari:DiffusionAnim:v1.0.0'
}
```

3.调用方法：

（1）在你将要跳转的界面下执行：
```Java
Intent intent = new Diffusion()
  /*
  .setFromEvent(Arrays.asList("BActivity", "CActivity"))  // 来自这个界面的动画不执行
  // 填写你的界面的名称，具体效果在下部分讲解
  
  .setGradientInnerColor("#66FFFFFF")  // 渐变扩散内的颜色
  .setGradientOuterColor("#FFFFFF")  // 渐变扩散外的颜色
  .setCurrentGradientRadius(1f)  // 梯度实际半径
  .setGradientSpread(0.7f)  // 渐变过渡范围因子
  .setScreenshotBitmap(null)  // 截图（null则取跳转前的根布局截图）
  */
  .start(this, BActivity.class);
if (intent != null) startActivity(intent);
overridePendingTransition(0, 0); // 注意：这是必备的，当然这是在如果你不想你的跳转动画难看的前提下
```

（2）在目标界面下执行：

```Java
// 如果你填写了这个界面的名称，下面将没用效果
new Diffusion().end(this, 1f, 0, () -> {
  // 动画结束后执行
});
```

# 关于
若有其他问题请在Issues中提出；
若长时间没用回答，可前往Telegram反馈群组：t.me/ColdrinkG

# 版本
v1.0
