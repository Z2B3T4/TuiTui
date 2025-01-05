package vip.xiaozhao.intern.baseUtil.controller.bookshelf;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import vip.xiaozhao.intern.baseUtil.controller.BaseController;
import vip.xiaozhao.intern.baseUtil.intf.DO.NovelDO;
import vip.xiaozhao.intern.baseUtil.intf.DO.HotNovelDO;
import vip.xiaozhao.intern.baseUtil.intf.constant.RedisConstant;
import vip.xiaozhao.intern.baseUtil.intf.dto.ResponseDO;
import vip.xiaozhao.intern.baseUtil.intf.entity.NovelInfo;
import vip.xiaozhao.intern.baseUtil.intf.service.NovelInfoService;
import vip.xiaozhao.intern.baseUtil.intf.service.SearchBookService;
import vip.xiaozhao.intern.baseUtil.intf.utils.redis.RedisUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/tuitui3/searchBook")
public class SearchBookController extends BaseController {
     /*
        区分主从在mapper层中，加了 @ReadOnly 是从库读取，没加的是默认主库操作
     */

    @Resource
    private SearchBookService searchBookService;

    @Resource
    private NovelInfoService novelInfoService;

    @GetMapping("/searchNovelList")
    public ResponseDO searchNovelList(@RequestParam(name = "page") int page,
                                      @RequestParam(name = "query") String query) {
        if(page < 1 || query.trim().isEmpty()) {
            return ResponseDO.fail("参数错误");
        }
        int pageSize = 10;
        int start = (page -1) * pageSize;
        List<NovelDO> novelDOS = searchBookService.searchNovelList(start,pageSize, query);
        return ResponseDO.success(novelDOS);
    }

    @PostMapping("/incrementNovelSearchNum/{novelId}")
    public ResponseDO incrementNovelSearchNum(@PathVariable(required = true) int novelId) {
        if(novelId < 1) {
            return ResponseDO.fail("参数错误");
        }
        HotNovelDO hotNovelByNovelId = searchBookService.getHotNovelByNovelId(novelId);
        if(hotNovelByNovelId == null){
            NovelInfo novelInfoByNovelId = novelInfoService.getNovelInfoByNovelId(novelId);
            if(novelInfoByNovelId == null){
                return ResponseDO.fail("小说不存在");
            }
            int id = novelInfoByNovelId.getId();
            String bookName = novelInfoByNovelId.getBookName();
            searchBookService.inserIntoHotBook(id,bookName);
        }else{
            searchBookService.incrementNovelSearchNum(novelId);
        }
        return ResponseDO.success(null);
    }



    @GetMapping("/getHotNovelList")
    public ResponseDO getHotNovelList() {
        String redisHotNovelList = RedisUtils.get(RedisConstant.HOT_NOVEL_LIST);
        if(redisHotNovelList != null){
            return ResponseDO.success(redisHotNovelList);
        }
        List<HotNovelDO> hotNovelList = searchBookService.getHotNovelList();
        RedisUtils.set(RedisConstant.HOT_NOVEL_LIST, hotNovelList,RedisUtils.EXRP_ONE_HOUR);
        if(hotNovelList.isEmpty()){
            return ResponseDO.success(null);
        }
        Collections.shuffle(hotNovelList);
        List<HotNovelDO> randomHotNovels = new ArrayList<>();
        int count = Math.min(10, hotNovelList.size());
        for (int i = 0; i < count; i++) {
            randomHotNovels.add(hotNovelList.get(i));
        }
        return ResponseDO.success(randomHotNovels);
    }



}
