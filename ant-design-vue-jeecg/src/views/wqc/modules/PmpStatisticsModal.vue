<template>
  <a-modal
    :title="title"
    :width="width"
    :visible="visible"
    :closable="false"
    :footer="null"
    :mask="false"
    @ok="close"
    @cancel="close">
    <a-spin :spinning="false">

      <div class="scroll-wrap">
        <a-list itemLayout="vertical" size="small" :dataSource="summaryData">
          <a-list-item
            slot="renderItem"
            slot-scope="item, index">

            <a-list-item-meta>

              <a slot="title">{{ item.title }}</a>
              <a-avatar slot="avatar" icon="schedule"/>
              <a class="list-content" slot="description" v-html="item.content" />

            </a-list-item-meta>

            <a-upload :fileList="item.annex">
            </a-upload>

          </a-list-item>
        </a-list>
      </div>

    </a-spin>
  </a-modal>
</template>

<script>

  import { httpAction } from '@/api/manage'
  import { deleteAction, getAction,downFile } from '@/api/manage'

  const debug = (msg) => {
    console.log('*************************');
    console.log('*************************');
    console.log(msg);
    console.log('*************************');
    console.log('*************************');
  }

  const getFileName = (path) => {
    if(path.lastIndexOf("\\")>=0){
      let reg=new RegExp("\\\\","g");
      path = path.replace(reg,"/");
    }
    return path.substring(path.lastIndexOf("/")+1);
  }

  export default {
    name: "PmpStatisticsModal",
    components: {
    },
    data() {
      return {
        title: '',
        width: 620,
        visible: false,
        summaryData: [],
        url: {
          query: "/summary/pmpSummary/list",
        }
      }
    },
    methods: {
      show(time, record) {
        this.visible = true;
        this.title = record.realName + "[" + time + "]";

        this.loadSummary(time, record.userName);
      },
      loadAnnex(annex) {
        let linkList = [];

        if ((null != annex) && (annex.length > 0)) {
          let annexList = annex.split(',');

          for(let idx in annexList) {
            linkList.push({
              uid: -1,
              name: getFileName(annexList[idx]),
              url: window._CONFIG['domianURL'] + "/sys/common/download/" + annexList[idx],
            });
          }
        }

        return linkList;
      },
      loadTitle(result) {
        return result.projectName + '-' + result.taskName + '[' +  result.createTime + ']';
      },
      loadSummary(time, userName) {
        this.summaryData = [];
        getAction(this.url.query, { summaryTime: time, owner: userName }).then((res) => {
          if (res.success) {
            for(let i = 0; i < res.result.total; ++i) {
              this.summaryData.push({
                title: this.loadTitle(res.result.records[i]),
                content: res.result.records[i].content,
                annex: this.loadAnnex(res.result.records[i].contentAnnex),
              });
            }
          }
        })
      },
      close () {
        this.visible = false;
      }
    }
  }

</script>

<style>
  .ant-popover-title {
    text-align: left;
    height: 30px;
    line-height: 30px;
  }

  .ant-card-body {
    padding: 0;
  }

  .ant-list-vertical .ant-list-item-meta {
    margin-bottom: 0px;
  }

  .ant-list-vertical .ant-list-item-content {
    display: block;
    margin: 0;
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
  }

  .scroll-wrap {
    width: 600px;
    margin: 0;
    padding: 0;
    max-height: 300px;
    overflow: hidden;
    overflow-y: scroll;
  }
</style>
