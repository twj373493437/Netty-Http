$(function(){
	
	var basepath = Utils.getBasePath();
	
	$.ajax({
		type:"post",
		url:basepath + "hello/bulls",
		async:true,
		data:{gg:2,name:'gggg', id:4},
		dataType:'text',
		success: function(data){
		    console.log(data);
		}
	});

	$.ajax({
		type:"post",
		url:basepath + "hello/bulls1",
		async:true,
		data:{gg:2,name:'gggg', id:4},
		dataType:'text',
		success: function(data){

		    console.log('bulls1', data);
		}
	});
})