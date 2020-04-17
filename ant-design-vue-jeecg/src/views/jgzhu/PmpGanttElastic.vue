<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :md="6" :sm="8">
            <a-form-item label="项目名称">
              <a-input placeholder="请输入项目名称" v-model="queryParam.projectname"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="项目分类">
              <j-dict-select-tag v-model="queryParam.projecttype" dictCode="project_type" />
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="日期">
              年
              <a-select
                style="width: 120px"
                v-model="queryParam.startyear"
              >
                <a-select-option
                  v-for="(item,index) in yearp"
                  :key="index"
                  :value="item.year"
                >{{item.year}}</a-select-option>
              </a-select>&nbsp;&nbsp;&nbsp;月
              <a-select style="width: 120px" allowClear v-model="queryParam.startmonth">
                <a-select-option value="01">01</a-select-option>
                <a-select-option value="02">02</a-select-option>
                <a-select-option value="03">03</a-select-option>
                <a-select-option value="04">04</a-select-option>
                <a-select-option value="05">05</a-select-option>
                <a-select-option value="06">06</a-select-option>
                <a-select-option value="07">07</a-select-option>
                <a-select-option value="08">08</a-select-option>
                <a-select-option value="09">09</a-select-option>
                <a-select-option value="10">10</a-select-option>
                <a-select-option value="11">11</a-select-option>
                <a-select-option value="12">12</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button
                type="primary"
                @click="searchReset"
                icon="reload"
                style="margin-left: 8px"
              >重置</a-button>
              <a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'" />
              </a>
            </span>
          </a-col>
        </a-row>
        <a-row :gutter="24" v-if="this.toggleSearchStatus">
          <a-col :md="6" :sm="8">
            <a-form-item label="项目状态">
              <j-dict-select-tag v-model="queryParam.status" dictCode="project_status" />
            </a-form-item>
          </a-col>

          <a-col :md="6" :sm="8">
            <a-form-item label="负责人">
              <j-select-multi-user v-model="queryParam.principal"></j-select-multi-user>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <div>
      <pmp-gantt-elastic-model v-if="showGantt" :dataSource="tasks"></pmp-gantt-elastic-model>
    </div>
    <div style="text-align:center">
      <span>正常完成</span>
      <a-rate :value="1" :count="1" style="color:#52c41a" />
      <span>延期完成</span>
      <a-rate :value="1" :count="1" style="color:orange" />
      <span>正常进行中</span>
      <a-rate :value="1" :count="1" style="color:#1890ff" />
      <span>延期进行中</span>
      <a-rate :value="1" :count="1" style="color:#FFD700" />
      <span>未开始</span>
      <a-rate :value="1" :count="1" style="color:#BABABA" />
    </div>
    <!-- <a-button @click="addTask" icon="plus">Add task</a-button> -->
  </a-card>
</template>

<style>
</style>

<script>
import moment from 'moment'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { deleteAction, getAction, putAction, downFile } from '@/api/manage'
import { filterObj, isContainPrincipal } from '@/utils/util'
import JDate from '@/components/jeecg/JDate.vue'
import { initDictOptions, filterDictText, myFilterMultiDictText } from '@/components/dict/JDictSelectUtil'
import JSelectMultiUser from '@/components/jeecgbiz/JSelectMultiUser'
import PmpGanttElasticModel from './modules/PmpGanttElasticModel'

const finishStyle = {
  base: {
    fill: '#52c41a',
    stroke: '#000000'
  }
}
const delayFinishStyle = {
  base: {
    fill: 'orange',
    stroke: '#000000'
  }
}
const doingStyle = {
  base: {
    fill: '#1890ff',
    stroke: '#000000'
  }
}
const delayDoingStyle = {
  base: {
    fill: '#FFD700',
    stroke: '#000000'
  }
}
const undoingStyle = {
  base: {
    fill: '#BABABA',
    stroke: '#000000'
  }
}

export default {
  name: 'PmpGanttElastic',
  mixins: [JeecgListMixin],
  components: {
    JSelectMultiUser,
    PmpGanttElasticModel
  },
  data() {
    return {
      yearp: [],
      //字典数组缓存-负责人
      principal: [],
      projectTypeDictOptions: [],
      taskTypeDictOptions: [],
      finishStyle,
      delayFinishStyle,
      doingStyle,
      delayDoingStyle,
      undoingStyle,
      tasks: [],
      showGantt: false,
      url: {
        list: '/protree/pmpProject/ganttList',
        listyaer: '/protree/pmpProject/getListYear'
      }
    }
  },
  created() {
    this.queryParam.startyear = new Date().getFullYear()
  },
  methods: {
    initDictConfig() {
      //初始化字典 - 创建人
      initDictOptions('sys_user,realname,username').then(res => {
        if (res.success) {
          this.principal = res.result
        }
      })
      //初始化字典 - 项目类型
      initDictOptions('task_type').then(res => {
        if (res.success) {
          this.taskTypeDictOptions = res.result
        }
      })
      //初始化字典 - 项目类型
      initDictOptions('project_type').then(res => {
        if (res.success) {
          this.projectTypeDictOptions = res.result
        }
      }),
        getAction(this.url.listyaer).then(res => {
          if (res.success) {
            this.yearp = res.result
          }
        })
    },
    openNotification(title, des) {
      this.$notification.open({
        message: title,
        description: des,
        icon: <a-icon type="frown" style="color: red" />
      })
    },
    //计算距离年末天数
    dateDiff(sDate) {
      var at = moment(sDate).format('YYYY-MM-DD')
      var to = moment(sDate)
        .endOf('year')
        .format('YYYY-MM-DD')
      return moment(to).diff(at, 'day') + 1
    },
    loadData(arg) {
      if (!this.url.list) {
        this.$message.error('请设置url.list属性!')
        return
      }
      //加载数据 若传入参数1则加载第一页的内容
      if (arg === 1) {
        this.ipagination.current = 1
      }
      var params = this.getQueryParams() //查询条件
      getAction(this.url.list, params).then(res => {
        if (res.success) {
          this.tasks = []
          for (var i = 0; i < res.result.length; i++) {
            var preList = []
            var temp = {}
            if (res.result[i].parentId == 'NULL') {
              temp = {
                id: res.result[i].id,
                label: res.result[i].taskname,
                user: myFilterMultiDictText(this.principal, res.result[i].principal),
                start:
                  res.result[i].startdate.length > 10 ? res.result[i].startdate.substr(0, 10) : res.result[i].startdate,
                duration:
                  res.result[i].duration > 365
                    ? this.dateDiff(res.result[i].startdate) * 24 * 60 * 60 * 1000
                    : res.result[i].duration * 24 * 60 * 60 * 1000,
                dayDuration: res.result[i].duration > 365 ? '>365' : res.result[i].duration,
                percent: res.result[i].schedule,
                realprojecttype: filterDictText(this.projectTypeDictOptions, res.result[i].projecttype),
                type: 'project',
                Status: res.result[i].status,
                photo: res.result[i].photo,
                projectname: res.result[i].projectname,
                taskname: res.result[i].taskname,
                projecttype: res.result[i].projecttype,
                projectcontent: res.result[i].projectcontent,
                principal: res.result[i].principal,
                participant: res.result[i].participant,
                schedule: res.result[i].schedule,
                startdate: res.result[i].startdate,
                enddate: res.result[i].enddate,
                emergencylevel: res.result[i].emergencylevel,
                projectmoney: res.result[i].projectmoney,
                remark: res.result[i].remark,
                annex: res.result[i].annex
              }
            } else {
              temp = {
                id: res.result[i].id,
                label: res.result[i].taskname,
                user: myFilterMultiDictText(this.principal, res.result[i].principal),
                start:
                  res.result[i].startdate.length > 10 ? res.result[i].startdate.substr(0, 10) : res.result[i].startdate,
                duration:
                  res.result[i].duration > 365
                    ? this.dateDiff(res.result[i].startdate) * 24 * 60 * 60 * 1000
                    : res.result[i].duration * 24 * 60 * 60 * 1000,
                dayDuration: res.result[i].duration > 365 ? '>365' : res.result[i].duration,
                percent: res.result[i].schedule,
                realprojecttype: filterDictText(this.taskTypeDictOptions, res.result[i].projecttype),
                Status: res.result[i].status,
                type: 'task',
                parentId: res.result[i].parentId,
                photo: res.result[i].photo,
                projectname: res.result[i].projectname,
                taskname: res.result[i].taskname,
                projecttype: res.result[i].projecttype,
                projectcontent: res.result[i].projectcontent,
                principal: res.result[i].principal,
                participant: res.result[i].participant,
                schedule: res.result[i].schedule,
                startdate: res.result[i].startdate,
                enddate: res.result[i].enddate,
                emergencylevel: res.result[i].emergencylevel,
                projectmoney: res.result[i].projectmoney,
                remark: res.result[i].remark,
                annex: res.result[i].annex
              }
            }
            // if (res.result[i].PreTaskId != '') {
            //   preList.push(resJson.Data[i].PreTaskId)
            //   temp.dependentOn = preList
            // }
            if (temp.Status == '1') {
              temp.style = this.finishStyle
            } else if (temp.Status == '2') {
              temp.style = this.delayFinishStyle
            } else if (temp.Status == '3') {
              temp.style = this.doingStyle
            } else if (temp.Status == '4') {
              temp.style = this.delayDoingStyle
            } else {
              temp.style = this.undoingStyle
            }
            this.tasks.push(temp)
          }
          // this.tasksUpdate(this.dataSource)
          if (this.tasks.length > 0) {
            console.log('tasks:', this.tasks)
            this.showGantt = true
          } else {
            this.openNotification('提示', ' 未查到相关数据！')
          }
        }
        if (res.code === 510) {
          this.$message.warning(res.message)
        }
      })
    },
    getQueryParams() {
      //获取查询条件
      let sqp = {}
      if (this.superQueryParams) {
        sqp['superQueryParams'] = encodeURI(this.superQueryParams)
      }
      var param = Object.assign(sqp, this.queryParam, this.isorter, this.filters)
      param.pageNo = this.ipagination.current
      param.pageSize = this.ipagination.pageSize
      return filterObj(param)
    }
  }
}
</script>