package vip.xiaozhao.intern.baseUtil.controller.bookshelf;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import vip.xiaozhao.intern.baseUtil.controller.BaseController;
import vip.xiaozhao.intern.baseUtil.intf.constant.CommonConstant;
import vip.xiaozhao.intern.baseUtil.intf.dto.ResponseDO;
import vip.xiaozhao.intern.baseUtil.intf.entity.YikeNovelBookshelf;
import vip.xiaozhao.intern.baseUtil.intf.service.BookShelfService;
import vip.xiaozhao.intern.baseUtil.intf.utils.JjwtUtil;
import vip.xiaozhao.intern.baseUtil.intf.utils.redis.RedisUtils;

import java.util.List;

@RestController
@RequestMapping("/tuitui3/bookshelf")
public class UserBookShelfController extends BaseController {
     /*
        区分主从在mapper层中，加了 @SlaveDataSource 是从库读取，可以指定从库的名称，没加的是默认主库操作
        或者加了名称是master的都是从主库读取
     */

    @Resource
    private BookShelfService bookShelfService;


    // 获取用户书架
    @GetMapping("/getBookShelfByUserId")
    public ResponseDO getBookShelfByUserId(HttpServletRequest request) throws Exception {
        int userId = getCurrentUserId(request);
        if(userId == -1){
            return ResponseDO.fail("您还未登录");
        }
        List<YikeNovelBookshelf> bookShelfBuUserId = bookShelfService.getBookShelfByUserId(userId);
        return ResponseDO.success(bookShelfBuUserId);
    }

    // 用户阅读章节
    @PostMapping("/readChapter")
    public ResponseDO readChapter(HttpServletRequest request,
                                  @RequestParam(value = "novelId", defaultValue = "-1") int novelId,
                                  @RequestParam(value = "chapterId", defaultValue = "-1") int chapterId) throws Exception {
        // 这里是用户读哪一章记录一下
        int userId = getCurrentUserId(request);
        if(userId == -1){
            return ResponseDO.fail("您还未登录");
        }
        System.out.println(chapterId);
        if(chapterId == -1 || novelId == -1){
            return ResponseDO.fail("参数错误");
        }
        bookShelfService.readChapter(userId,novelId,chapterId);
        return ResponseDO.success(null);
    }

    // 用户置顶/非置顶小说
    @PutMapping("/updateTopBook/{novelId}")
    public ResponseDO updateTopBook(HttpServletRequest request,
                                    @PathVariable(required = true) int novelId) throws Exception {
        int userId = getCurrentUserId(request);
        if(userId == -1){
            return ResponseDO.fail("您还未登录");
        }
        bookShelfService.updateTopBook(userId,novelId);
        return ResponseDO.success(null);
    }

    // 用户删除小说
    // 同时也是取消订阅
    @DeleteMapping("/delete/{novelId}")
    public ResponseDO deleteBookByUserIdAndNovelId(HttpServletRequest request,
                                                   @PathVariable(required = true) int novelId) throws Exception {
        int userId = getCurrentUserId(request);
        if(userId == -1){
            return ResponseDO.fail("您还未登录");
        }
        bookShelfService.deleteBookByUserIdAndNovelId(userId,novelId);
        return ResponseDO.success(null);
    }

    // 订阅小说
    @PostMapping("/subscribe/{novelId}")
    public ResponseDO subscribeNovel(HttpServletRequest request,@PathVariable(required = true) int novelId) throws Exception {
        int userId = getCurrentUserId(request);
        if(userId == -1){
            return ResponseDO.fail("您还未登录");
        }
        bookShelfService.subscribeNovel(userId,novelId);
        return ResponseDO.success(null);
    }

    // 这里是用户点开阅读了变为已读
    @PutMapping("/updateUserIsRead")
    public ResponseDO updateUserIsRead(HttpServletRequest request,
                                   @RequestParam(value = "novelId",defaultValue = "-1")int novelId) throws Exception {
        int userId = getCurrentUserId(request);
        if(userId == -1){
            return ResponseDO.fail("您还未登录");
        }
        if(novelId == -1) {
            return ResponseDO.fail("参数错误");
        }
        bookShelfService.updateIsReadByUserIdAndNovelId(userId,novelId);
        return ResponseDO.success(null);
    }

    // 将这本小说对应的用户改为未读,这个是根据一本小说id进行修改用户未读状态
    @PutMapping("/updateUserIsReadByNovelID/{novelId}")
    public ResponseDO updateUserIsReadByNovelID(HttpServletRequest request,@PathVariable(required = true)int novelId) throws Exception {
        int userId = getCurrentUserId(request);
        if(userId == -1){
            return ResponseDO.fail("您还未登录");
        }
        bookShelfService.updateIsReadByNovelId(userId,novelId);
        return ResponseDO.success(null);
    }

    // 这个是根据传过来的一组小说id来修改用户的未读状态
    @PutMapping("/updateUserIsReadByNovelIDs")
    public  ResponseDO updateUserIsReadByNovelIDs(HttpServletRequest request,
                                                 @RequestBody List<Integer> novelIds) throws Exception {
        int userId = getCurrentUserId(request);
        if(userId == -1){
            return ResponseDO.fail("您还未登录");
        }
        bookShelfService.updateIsReadByNovelIdList(userId,novelIds);
        return ResponseDO.success(null);
    }



}
