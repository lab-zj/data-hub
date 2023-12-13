<template>
  <el-dialog
    :title="isVerifiedPhone ? '密码更改' : '手机验证'"
    :visible="forgetPasswordModalVisible"
    :confirm-loading="false"
    :ok-text="isVerifiedPhone ? '确定' : '验证'"
    cancel-text="取消"
    @ok="handleForgetPasswordModalOk"
    @cancel="closeForgetPasswordModal"
  >
    <el-form
      :form="form"
      @submit="handleForgetPasswordModalOk"
      @submit.native.prevent
    >
      <div v-if="!isVerifiedPhone">
        <p style="letter-spacing: 1px; margin-bottom: 24px;">
          为了确保您为本人操作，请先进行安全验证。
        </p>
        <p style="color: #5d637e; font-weight: 500; letter-spacing: 1px;">
          验证方式
        </p>
        <el-form-item>
          <el-input
            v-decorator="[
              'phone',
              {
                rules: [{ required: true, validator: validatePhone }],
              },
            ]"
            style="height: 40px; margin: 0 0 5px;"
            placeholder="请输入手机号"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <div style="display: flex;">
            <el-input
              v-decorator="[
                'phoneCaptcha',
                {
                  rules: [
                    { required: true, message: '请输入您的手机验证码！' },
                  ],
                },
              ]"
              style="height: 40px; margin: 5px 0;"
              placeholder="请输入6位验证码"
            />
            <el-button
              v-show="show"
              style="height: 40px; margin: 5px; margin-right: 0;"
              @click="sendCaptcha"
              >发送验证码</el-button
            >
            <span
              v-show="!show"
              slot="addonAfter"
              style="
                height: 40px;
                line-height: 1.5;
                margin: 5px;
                margin-right: 0;
                width: 100px;
              "
              disabled
            >
              {{ count }}秒之后可以重新发送
            </span>
          </div>
          <div
            v-if="captchaCode !== ''"
            style="display: flex; margin-bottom: 8px;"
          >
            <el-input
              v-model="captcha"
              style="height: 40px; margin: 0 0 5px;"
              placeholder="请输入验证码"
            ></el-input>
            <img
              style="height: 40px; margin-left: 5px; width: 102px;"
              alt=""
              :src="`data:text/css;base64,${captchaCode}`"
              @click="refreshCaptchaCode"
            />
          </div>
        </el-form-item>
        <p
          style="
            color: #222432;
            font-size: 12px;
            letter-spacing: 1.12px;
            margin-bottom: 2px;
          "
        >
          没收到短信验证码？
        </p>
        <p
          style="
            color: #222432;
            font-size: 12px;
            letter-spacing: 1.12px;
            line-height: 18px;
            opacity: 0.5;
          "
        >
          1、网络通讯异常可能会造成短信丢失，请重新获取或稍后再试。<br />
          2、请核实手机是否已欠费停机，或者屏蔽了系统短信。<br />
          <!-- 3、如果手机已丢失或停用，请选择其他验证方式。 -->
        </p>
      </div>
      <div v-if="isVerifiedPhone">
        <p style="color: #5d637e; font-weight: 500; letter-spacing: 1px;">
          新密码
        </p>
        <el-form-item>
          <el-input-password
            v-decorator="[
              'password1',
              {
                rules: [{ validator: validateToRepeatPassword }],
              },
            ]"
            type="password"
            style="height: 40px; margin: 0 0 10px;"
            placeholder="8-16位 至少包含数字、大写字母、小写字母、特殊字符中的三种类型"
            @focusin="inputFocus('password', 'in')"
            @focusout="inputFocus('password', 'out')"
          ></el-input-password>
        </el-form-item>
        <el-form-item v-if="focus === 'password' || hover === 'password'">
          <template v-for="(passwordRule, index) in passwordRuleList" :key="`rule-${index}`">
            <div
              :class="passwordRule.rule ? 'success-password-tips' : 'error-password-tips'"
            >
              <el-icon
                theme="filled"
                style="margin-right: 6px;"
                :type="passwordRule.rule ? 'check-circle' : 'close-circle'"
              />{{ passwordRule.text }}
            </div>
          </template>
        </el-form-item>
        <el-form-item>
          <el-input-password
            v-decorator="[
              'password2',
              {
                rules: [{ required: true, validator: checkRepeatPassword }],
              },
            ]"
            type="password"
            style="height: 40px; margin: 0 0 10px;"
            placeholder="请确认新密码"
          ></el-input-password>
        </el-form-item>
      </div>
      <el-form-item>
        <el-button type="primary" html-type="submit" style="display: none;"
          >登录</el-button
        >
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
import { getPhoneCaptcha, getImageCaptchaCode, verifyPhone } from '@/api/sms'
import { modifyPassword } from '@/api/user'

export default {
  name: 'ForgetPassword',
  components: { },
   props: {
    forgetPasswordModalVisible: {
      type:Boolean,
      default:false
    },
    phone: {
      type:String,
      default:''
    },
  },
  data () {
    return {
      form:$form.createForm(this),
      phoneBuffer:'',// 表单被隐藏时 数据会被清空 用这个暂存手机号
      captchaCode:'',// 图片验证码base64编码
      captcha:'',// 图片验证码输入
      isVerifiedPhone:false,// 是否已经验证手机
      focus:'',// focus的input
      hover:'' ,// hover的input
      passwordRuleList: [
        {
          rule: false,
          text: '8-16个字符',
        },
        {
          rule: false,
          text: '包含数字',
        },
        {
          rule: false,
          text: '包含小写字母',
        },
        {
          rule: false,
          text: '数字、小写字母、大写字母、特殊字符至少包含3种',
        },
      ],
      show: true,
      count: 0,
      timer: null,
    }
  },
  computed: {

  },
  mounted () {

  },
  created() {
    // $emit("handleTab", 3);
  },
   beforeCreate() {
    form = $form.createForm(this, { name: 'password_form' })
  },
  methods: {
    resetModalValue() {
      form.setFieldsValue({
        phone: '',
        phoneCaptcha: '',
      })
      if (isVerifiedPhone) {
        form.setFieldsValue({
          password1: '',
          password2: '',
        })
      }
      captcha = ''
      captchaCode = ''
      $emit('close-forget-password-modal')
      isVerifiedPhone = false
    },

  
    closeForgetPasswordModal() {
      resetModalValue()
      $emit('close-forget-password-modal')
    },

    
    async sendCaptcha() {
      // 间隔60秒
      const TIME_COUNT = 60
      const result = await getPhoneCaptcha(
        captcha === ''
          ? {
              data: { phone: form.getFieldValue('phone') },
            }
          : {
              data: {
                phone: form.getFieldValue('phone'),
                captchaCode: captcha,
              },
            }
      )
      // 根据返回code决定前端
      if (result.data.code === 100) {
        $message.success('短信发送成功')
        // 发送成功，设置60秒间隔才能发送
        count = TIME_COUNT
        show = false
        timer = setInterval(() => {
          if (count > 0 && count <= TIME_COUNT) {
            count -= 1
          } else {
            show = true
            clearInterval(timer)
            timer = null
          }
        }, 1000)
      } else if (
        // 需要请求验证码的情况
        result.data.code === 20016 ||
        result.data.code === 20015 ||
        captchaCode !== ''
      ) {
        $message.error(result.data.message)
        const captchaResult = await getImageCaptchaCode({
          data: form.getFieldValue('phone'),
        })
        captchaCode = captchaResult.data.result
      } else {
        $message.error(result.data.message)
      }
    },

    // 刷新图片验证码
    async refreshCaptchaCode() {
      const result = await getImageCaptchaCode({
        data: form.getFieldValue('phone'),
      })
      captchaCode = result.data.result
    },

    async handleForgetPasswordModalOk() {
      if (isVerifiedPhone) {
        form.validateFields(
          ['password1', 'password2'],
          async (error, values) => {
            if (!error) {
              const result = await modifyPassword({
                data: {
                  phone: phoneBuffer,
                  password1: values.password1,
                  password2: values.password2,
                },
              })
              if (result.data.code === 100) {
                resetModalValue()
              } else {
                $message.error(result.data.message)
              }
            } else {
              console.log(error)
            }
          }
        )
      } else {
        form.validateFields(
          ['phone', 'phoneCaptcha'],
          async (error, values) => {
            if (!error) {
              const result = await verifyPhone({
                data: values.phone,
                params: { code: values.phoneCaptcha },
              })
              if (result.data.code === 100) {
                phoneBuffer = form.getFieldValue('phone')
                isVerifiedPhone = true
              } else {
                $message.error(result.data.message)
              }
            } else {
              console.log(error)
            }
          }
        )
      }
    },

    // 重复输入密码rule
    checkRepeatPassword(rule, value, callback) {
      if (value !== form.getFieldValue('password1')) {
        callback('两次密码输入不一致！')
      }
      callback()
    },

    // password 和 repeat password 校验
    validateToRepeatPassword(rule, value, callback) {
      // 测试密码长度是否为8-16位
      if (/[\S\s]{8,16}/.test(value)) {
        passwordRuleList[0].rule = true
      } else {
        passwordRuleList[0].rule = false
      }
      // 测试密码是否包含数字
      if (/[\d.]/.test(value)) {
        passwordRuleList[1].rule = true
      } else {
        passwordRuleList[1].rule = false
      }
      // 测试密码是否包含小写数字
      if (/[.a-z]/.test(value)) {
        passwordRuleList[2].rule = true
      } else {
        passwordRuleList[2].rule = false
      }
      // 测试密码是否符合全部规则至少包含数字、大写字母、小写字母、特殊字符中的三种类型
      if (
        /^(?![A-Za-z]+$)(?![\dA-Z]+$)(?![\WA-Z_]+$)(?![\da-z]+$)(?![\W_a-z]+$)(?![\W\d_]+$)[\W\w]{3,}$/.test(
          value
        )
      ) {
        passwordRuleList[3].rule = true
      } else {
        passwordRuleList[3].rule = false
      }

      // 测试密码是否符合全部规则
      if (
        !/^(?![A-Za-z]+$)(?![\dA-Z]+$)(?![\WA-Z_]+$)(?![\da-z]+$)(?![\W_a-z]+$)(?![\W\d_]+$)[\W\w]{8,16}$/.test(
          value
        )
      ) {
        callback(
          '请输入8-16位字符，至少包含数字、大写字母、小写字母、特殊字符中的三种类型'
        )
      }
      if (value && form.getFieldValue('repeatPassword')) {
        form.validateFields(['repeatPassword'], { force: true })
      }
      callback()
    },

    // 手机号格式rule
    validatePhone(rule, value, callback) {
      if (!value) {
        callback('请输入您的手机号！')
      } else if (!/^1[,3-57-9]\d{9}$/.test(value)) {
        callback('手机号格式错误!')
      } else if (
        phone &&
        !isVerifiedPhone &&
        phone !== form.getFieldValue('phone')
      ) {
        callback('请输入绑定的手机号')
      } else {
        callback()
      }
    },

    inputFocus(item, type) {
      switch (type) {
        case 'in':
          focus = item
          break
        case 'out':
          focus = ''
          break
        default:
      }
    },

    inputHover(item, type) {
      switch (type) {
        case 'in':
          hover = item
          break
        case 'out':
          hover = ''
          break
        default:
      }
    },
  },
}
</script>

<style lang="less" scoped>
// 行间距
.ant-form-item {
  margin-bottom: 0;
}

.success-password-tips {
  color: #52c41a;
}

.error-password-tips {
  color: #7e817d;
}
</style>
