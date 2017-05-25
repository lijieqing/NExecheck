package com.kstech.nexecheck.domain.config.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2016/10/25.
 */

public class ResourceVO {

    private MsgVO[] msgArray ;
//	private MsgVO[] imageArray ;

    private Map<String, MsgVO> msgMap = new HashMap<String, MsgVO>();

    private Map<String, MsgVO> msgMapUKRefName = new HashMap<String, MsgVO>();

    private Map<String, ImageVO> imageMap = new HashMap<String, ImageVO>();

    public void setMsgArray(List<MsgVO> msgArray) {
        this.msgArray = msgArray.toArray(new MsgVO[msgArray.size()]);
    }

    public MsgVO[] getMsgArray() {
        return msgArray;
    }

    public MsgVO getMsgVO(short index){
        return msgArray[index];
    }

    public Short getMsgIndex(String id) {
        MsgVO msgVO = msgMap.get(id);
        return msgVO == null ? null : msgVO.getIndex();
    }

    public Short getImageIndex(String id) {
        ImageVO imgVO = imageMap.get(id);
        return imgVO == null ? null : imgVO.getIndex();
    }

    public void putMsg(MsgVO msgVO) {
        msgMap.put(msgVO.getId(), msgVO);
        msgMapUKRefName.put(msgVO.getRefName(), msgVO);
    }

    public MsgVO getMsg(String idOrRefName){
        return msgMap.get(idOrRefName)==null?msgMapUKRefName.get(idOrRefName):msgMap.get(idOrRefName);
    }

    public void putImage(String id, String refName, String content, short index) {
        imageMap.put(id, new ImageVO(id, refName, content, index));
    }

    public static class MsgVO implements Serializable{
        private static final long serialVersionUID = -5534144601502742909L;
        protected short index;
        private String id;
        private String refName;
        private String content;

        /**
         *
         * @param id
         *            该字符串的索引
         * @param refName
         *            该字符串的引用名称
         * @param content
         *            字符串内容
         * @param index
         *            在配置文件中的位置
         */
        public MsgVO(String id, String refName, String content, short index) {
            this.id = id;
            this.refName = refName;
            this.content = content;
            this.index = index;
        }

        public String getId() {
            return id;
        }

        public String getRefName() {
            return refName;
        }

        public String getContent() {
            return content;
        }

        public short getIndex() {
            return index;
        }

        public void setIndex(short index) {
            this.index = index;
        }

    }

    public static class ImageVO extends MsgVO {

        /**
         *
         * @param id
         *            该图片的索引
         * @param refName
         *            该图片的引用名称
         * @param content
         *            图片文件名（全路径或相对于检测App安装目录的路径
         * @param index
         *            在配置文件中的位置
         */
        public ImageVO(String id, String refName, String content, short index) {
            super(id, refName, content, index);
        }

    }

}
