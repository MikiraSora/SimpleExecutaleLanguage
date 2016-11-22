#简介
	
此项目(简称SEL)是另一个项目[ExtrameCalculator](https://github.com/MikiraSora/ExtrameFunctionCalculator)的子项目。主要用于作为那项目计算器的脚本语言，能给用户动态加载/卸载所预设写好的函数文件，因此本项目暂时不能分离独立于父项目。

---

##SEL语法
###预处理
#### #package <脚本名>
此命令必须存在并在脚本内容开头，会作为此脚本的名字，当某个脚本跨脚本查询引用函数时候可以以此为依据。用户也可以通过Calculator::GetScriptManager()::UnloadScript(<脚本名>)等方式来卸载此脚本文件。
#### #version <浮点数>
此命令可选并在脚本内容开头，会作为此脚本的版本号，建议整数部分作为正式版本号而小数部分作为修改或测试标记。
#### #include <绝对路径/相对路径>
此命令可选，建议放在脚本内容开头，首先解析器会检查指定加载的脚本是否已经存在,如果没有才会真正的读取加载，如果有的话就跳过此命令。如果是"./xxx.cml"就是相对路径，会以当前的加载的脚本文件路径目录为基准。比如要加载的脚本文件是"g:\mymath\stdmath.cml"，那么#include ./stdmath_2.cml的实际路径就是"g:\mymath\stdmath_2.cml"；绝对路径就是普通的完整的路径，如"g:\mymath\mat\stdmat.cml"。

###基本语法
(注意:此脚本语言无大括号，而是用xxx-endxxx为标记)
####function-return-endfunction
    function getNewValue(a,b)
	    set a=a*10+b
	    return a
	endfunction
函数声明的参数无任何修饰，只有名字，调用时必须传递的参数需数量一致。
每个function至少有一个return且必须是可以被执行到的,因为function的设定是"有什么就必然返回什么“。

####set
    set a=a+1
右边的值必然会计算成常量在赋值给左边的变量

####call <command\>

    call reg dymFunc(x)=x^5-1
call会GetCalculator.Execute(command),也即是说刚才那段代码是在自己绑定的计算器中动态声明了一个函数.

#### if-(then)-else-endif
    ....
    if(a>10)
	    set a=a+1
		set b=b+6666
	else
		set a=a-1
		set b=b-6666
	endif
	....

####loop-(continue)-(break)-endloop

    loop
	    set a=a+1
	    if(a>5)
		    break
		else
			continue
			set a=a-1
		endif
	endloop
PS:语句set a=a-1永远执行不了,just you know.

###示例
####脚本语言
g:\stdmath2.cml  :

    #package stdmath2
    #version 1.2857
    
    #include ./stdmath.cml
    
    function getValueB(a)
	    return getValueA(a)
	endfunction
	
	function getLoop(a)
		loop
			set a=a+1
			if(a>10)
				break
			endif
		endloop
		return a
	endfunction
	
	function getRecursion(a)
		if(a==0)
			return a
		else
			return a+getRecursion(a-1)
		endif
	endfunction
	
	function scriptFileTest()
		set result=getValueB(10)
		call solve result
		
		set result=getLoop(1)
		call solve result
		
		set result=getRecursion(555555)
		call solve result
		
		return 0
	endfunction

然后在你的代码就这么写:

    Calculator calculator=new Calculator();
    
    calculator.LoadScript("g:\stdmath2.cml");
    calculator.Solve("scriptFileTest()");
    /*上面两句等同于
    calculator.Execute("load_script g:\stdmath.cml");
    calcilator.Execute("solve scriptFileTest()");
    */
#其他
###已存在的一些问题
* 变量查询引用顺序混乱
* 函数调用过程繁杂
* 偶尔的执行过程出现错误
* 作者是非洲人

###计划实装的内容
* 注释
* 解析缓存
* 部分计算优化成常量表示
* 语言脱离父项目
* 脱非入欧
