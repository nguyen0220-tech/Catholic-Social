package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.service.hepler.EntityUtils;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.entity.dto.SearchDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.SearchRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.Search;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.SearchMapper;
import com.catholic.ac.kr.catholicsocial.projection.SearchProjection;
import com.catholic.ac.kr.catholicsocial.repository.SearchRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SearchRepository searchRepository;
    private final UserRepository userRepository;

    public ListResponse<SearchDTO> getAllByUser(Long userId, int page, int size) {
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<SearchProjection> searchProjections = searchRepository.findAllByUser(user, pageable);

        List<SearchProjection> list = searchProjections.getContent();

        List<SearchDTO> result = SearchMapper.searchDTO(list);

        return new ListResponse<>(result, new PageInfo(page, size, searchProjections.hasNext()));
    }

    public GraphqlResponse<String> createSearch(Long userId, SearchRequest request){
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");

        Search newSearch = new Search();
        newSearch.setUser(user);
        newSearch.setKeyword(request.getKeyword());

        searchRepository.save(newSearch);

        return GraphqlResponse.success("search success", null);
    }

    public GraphqlResponse<String> deleteSearch(Long userId, Long searchId){
        User user = EntityUtils.getOrThrow(userRepository.findById(userId), "User");

        Search search = EntityUtils.getOrThrow(searchRepository.findByIdAndUser(searchId, user), "Search");

        searchRepository.delete(search);

        return GraphqlResponse.success("deleted search success", null);
    }
}
