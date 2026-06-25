package com.poly.models.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.poly.models.entities.Account;
import com.poly.models.entities.Comment;
import com.poly.models.entities.Product;
import com.poly.models.repositories.CommentRepository;
import com.poly.models.requests.CommentRequest;
import com.poly.models.responses.CommentResponse;

import jakarta.persistence.EntityNotFoundException;


@Mapper(componentModel = "spring", uses = {ReplyMapper.class})
public abstract class CommentMapper {

	@Autowired
	protected CommentRepository commentRepo;

	@Mapping(target = "createdDate", 		ignore = true)
	@Mapping(target = "product",     		ignore = true)
	@Mapping(target = "account",     		ignore = true)
	@Mapping(target = "replies",     		ignore = true)
	@Mapping(target = "deleted",     		ignore = true)
	public abstract Comment toEntity(CommentRequest request); 
	
	@Mapping(target = "createdDate", 				source = "createdDate", 			dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target =  "username", 			source = "account.username")
	@Mapping(target =  "fullname", 			source = "account.fullname")
	@Mapping(target =  "photo", 			source = "account.photo")
	@Mapping(target = "productPk", 			source = "product.pk")
	@Mapping(target = "accountPk", 			source = "account.pk")
	@Mapping(target = "replyResponses", 	ignore = true)
	@Named("basicResponse")
	public abstract CommentResponse toBasicResponse(Comment comment);

	@Mapping(target = "createdDate", 				source = "createdDate", 			dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target =  "username", 			source = "account.username")
	@Mapping(target =  "fullname", 			source = "account.fullname")
	@Mapping(target =  "photo", 			source = "account.photo")
	@Mapping(target = "productPk", 			source = "product.pk")
	@Mapping(target = "accountPk", 			source = "account.pk")
	@Mapping(target = "replyResponses", 	source = "replies")
	@Named("detailedResponse")
	public abstract CommentResponse toDetailedResponse(Comment comment);

	@Named("basicCommentResponseList")
	@IterableMapping(qualifiedByName = "basicResponse")
	public abstract List<CommentResponse> toBasicResponseList(List<Comment> comments);
	
	@Named("detailedCommentResponseList")
	@IterableMapping(qualifiedByName = "detailedResponse")
	public abstract List<CommentResponse> toDetailedResponseList(List<Comment> comments);

	protected void afterToEntity(CommentRequest request, @MappingTarget Comment comment) {
		Long pk = comment.getPk();
		if (pk != null) {
			Comment oldComment = commentRepo.findById(pk).orElseThrow(() -> new EntityNotFoundException("Comment not found with pk: " + pk));
			comment.setCreatedDate(oldComment.getCreatedDate());
			comment.setProduct(oldComment.getProduct());
			comment.setAccount(oldComment.getAccount());
			comment.setReplies(oldComment.getReplies());
			comment.setDeleted(oldComment.getDeleted());
			return;
		};
		comment.setCreatedDate(LocalDateTime.now());
		Product product = new Product();
		product.setPk(request.getProductPk());
		Account account = new Account();
		account.setPk(request.getAccountPk());
		comment.setProduct(product);
		comment.setAccount(account);
		comment.setDeleted(false);
	}

}
