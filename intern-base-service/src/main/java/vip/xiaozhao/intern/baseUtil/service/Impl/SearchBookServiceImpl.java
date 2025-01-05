package vip.xiaozhao.intern.baseUtil.service.Impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaozhao.intern.baseUtil.intf.DO.NovelDO;
import vip.xiaozhao.intern.baseUtil.intf.DO.HotNovelDO;
import vip.xiaozhao.intern.baseUtil.intf.mapper.SearchBookMapper;
import vip.xiaozhao.intern.baseUtil.intf.service.NovelInfoService;
import vip.xiaozhao.intern.baseUtil.intf.service.SearchBookService;

import java.util.List;

@Service
public class SearchBookServiceImpl implements SearchBookService {
      /*
        区分主从在mapper层中，加了 @SlaveDataSource 是从库读取，可以指定从库的名称，没加的是默认主库操作
        或者加了名称是master的都是从主库读取
     */

    @Resource
    private SearchBookMapper searchBookMapper;

    @Resource
    private NovelInfoService novelInfoService;
    @Override
    public List<NovelDO> searchNovelList(int start,int pageSize,String query){

        return searchBookMapper.searchNovelList(start,pageSize,query);
    }
    @Override
    public void inserIntoHotBook(int id,String bookName){
        searchBookMapper.inserIntoHotBook(id,bookName);
    }
    @Override
    public HotNovelDO getHotNovelByNovelId(int novelId){
        return searchBookMapper.getHotNovelByNovelId(novelId);
    }


    @Override
    public void incrementNovelSearchNum(int novelId){
        searchBookMapper.incrementNovelSearchNum(novelId);
    }
    @Override
    public List<HotNovelDO> getHotNovelList() {
        List<HotNovelDO> hotNovelList = searchBookMapper.getHotNovelList();
        return hotNovelList;
    }

}
