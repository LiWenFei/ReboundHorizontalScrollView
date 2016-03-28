ReboundHorizontalScrollView

ReboundHorizontalScrollView是一个左侧，右侧回弹效果的控件


//以下是实现这个控件的自定义监听事件
reboundHorizontalScrollView.setOnReboundListtener(new ReboundHorizontalScrollView.OnReboundListener() {
            @Override
            public void OnLeftRebound() {
                Toast.makeText(MainActivity.this, "触发左侧事件", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnRightRebound() {
                Toast.makeText(MainActivity.this, "触发右侧事件", Toast.LENGTH_SHORT).show();
            }
        });