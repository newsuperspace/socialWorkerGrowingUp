<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<head>
  <title>测试WeUI上传照片</title>
  <!-- Required meta tags -->
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

  <!-- Bootstrap CSS -->
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/css/bootstrap.min.css" integrity="sha384-Smlep5jCw/wG7hdkwQ/Z5nLIefveQRIY9nfy6xoR1uRYBtpZgI6339F5dgvm/e9B"
    crossorigin="anonymous">
  <link rel="stylesheet" href="https://res.wx.qq.com/open/libs/weui/1.1.3/weui.min.css">
</head>

<body>

  <div class="weui-gallery" id="gallery">
    <span class="weui-gallery__img" id="galleryImg"></span>
    <div class="weui-gallery__opr">
      <a href="javascript:" rel="external nofollow" class="weui-gallery__del">
        <i class="weui-icon-delete weui-icon_gallery-delete"></i>
      </a>
    </div>
  </div>


  <div class="weui-cells weui-cells_form" id="uploader">
    <div class="weui-cell">
      <div class="weui-cell__bd">
        <div class="weui-uploader">
          <div class="weui-uploader__hd">
            <p class="weui-uploader__title">图片上传</p>
            <div class="weui-uploader__info"><span id="uploadCount">0</span>/5</div>
          </div>
          <div class="weui-uploader__bd">
            <ul class="weui-uploader__files" id="uploaderFiles">


            </ul>
            <div class="weui-uploader__input-box">
              <input id="uploaderInput" class="weui-uploader__input" type="file" accept="image/*" capture="camera"
                multiple />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <a name="getImg" id="getImg" class="btn btn-sucesss" href="#" role="button" onclick="getImg();">获取</a>

  <!-- Optional JavaScript -->
  <!-- jQuery first, then Popper.js, then Bootstrap JS -->
  <script src="${pageContext.request.contextPath}/js/jquery-3.2.0.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js" integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"
    crossorigin="anonymous"></script>
  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/js/bootstrap.min.js" integrity="sha384-o+RDsa0aLu++PJvFqy8fFScvbHFLtbvScb8AjopnFD+iEQ7wo/CG0xlczd+2O/em"
    crossorigin="anonymous"></script>
  <script type="text/javascript" src="https://res.wx.qq.com/open/libs/weuijs/1.1.4/weui.min.js"></script>
  <script type="text/javascript">
    function getImg() {
      $input = $("#uploaderInput");
      console.log($input[0].files);
    }

    $(function () {

      // 通过onBeforeQueued()校验的允许上传的图片的数量
      var uploadCount = 0;
      // 用来记录上传文件信息的全局属性
      var  fileName  = "";



      weui.uploader('#uploader', {
        url: 'http://localhost/grow/tryAction_upload.action',
        auto: false,
        type: 'file',
        fileVal: 'file',
        compress: {
          width: 1600,
          height: 1600,
          quality: .8
        },

        // 文件添加前的回调，return false则不添加，通常我们会把格式校验添加到这里
        onBeforeQueued: function (files) {
          // `this` 是轮询到的文件, `files` 是所有文件

          if (["image/jpg", "image/jpeg", "image/png", "image/gif"].indexOf(this.type) < 0) {
            weui.alert('请上传图片');
            return false; // 阻止文件添加
          }
          if (this.size > 10 * 1024 * 1024) {
            weui.alert('请上传不超过10M的图片');
            return false;
          }
          if (files.length > 5) { // 防止一下子选择过多文件
            weui.alert('最多只能上传5张图片，请重新选择');
            return false;
          }
          if (uploadCount + 1 > 5) {
            weui.alert('最多只能上传5张图片');
            return false;
          }
          // 校验成功，计数器+1
          ++uploadCount;
          $("#uploadCount").text(uploadCount);
          return true; // 返回false会阻止默认行为，不插入预览图的框架
        },

        // 文件添加成功的回调,this包含刚刚成功通过onBeforeQueued校验允许上传的图片文件的全部关键信息,其中包含的信息如下：
        // id: 1
        // lastModified: 1534309141540
        // lastModifiedDate: Wed Aug 15 2018 12: 59: 01 GMT + 0800(中国标准时间) {}
        // name: "“互联网+社会工作” 保障社区志愿服务安全开展 项目结构.png"
        // size: 85094
        // status: "ready"
        // stop: ƒ()
        // type: "image/png"
        // upload: ƒ()
        // url: "blob:null/32f13f99-d241-4575-aee8-36ee016ce587"
        onQueued: function () {
          console.log(this);

          // console.log(this.status); // 文件的状态：'ready', 'progress', 'success', 'fail'
          // console.log(this.base64); // 如果是base64上传，file.base64可以获得文件的base64
          var u = this.url;
          console.log("url=" + u);
          var dom =
            '<li class="weui-uploader__file weui-uploader__file_status" style="background-image:url(@url@)" id="' +
            'li' + this.id + '"><div  class="weui-uploader__file-content">50%</div></li>';
          dom.replace('@url@',u);
          var $dom = $(dom);
          $dom.find("div").text("0" + "%");
          $("#uploaderFiles").prepend($dom);
          
          fileName = this.name;
          console.log("上传的文件名："+fileName);

          this.upload(); // 如果是手动上传，这里可以通过调用upload来实现；也可以用它来实现重传。
          // this.stop(); // 调用中断上传的方法

          // return true; // 阻止默认行为，不显示预览图的图像
        },

        // 文件上传前调用
        onBeforeSend: function (data, headers) {
          console.log(this, data, headers);
          // $.extend(data, { test: 1 }); // 可以扩展此对象来控制上传参数
          // $.extend(headers, { Origin: 'http://127.0.0.1' }); // 可以扩展此对象来控制上传头部
          $.extend(headers, {
            'Access-Control-Allow-Origin': '*',
          });

          $.extend(data,{
            fileName: fileName,
          });
          // return false; // 阻止文件上传
        },

        // 上传进度的回调
        onProgress: function (procent) {
          console.log(this, procent);
          // return true; // 阻止默认行为，不使用默认的进度显示
        },

        // 上传成功的回调
        onSuccess: function (ret) {
          console.log(this, ret);
          // return true; // 阻止默认行为，不使用默认的成功态
        },

        // 上传失败的回调
        onError: function (err) {
          console.log(this, err);
          // return true; // 阻止默认行为，不使用默认的失败态
        }
      });
    });

  </script>
</body>
</html>
