$(document).ready(function(){
  $('.quote-carousel').slick({
    arrows: true,
    autoplay: true,
    dots:true,
    centerMode:false,
    infinite: true
  });
$('.tutorial-carousel').slick({
    arrows: true,
    dots:true,
    autoplay: true,
    autoplaySpeed: 6000,
    slidesToShow: 1,
    centerMode:true,
    centerPadding:'80px',
    infinite: true
  });
});
