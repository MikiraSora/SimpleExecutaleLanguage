#���
	
����Ŀ(���SEL)����һ����Ŀ[ExtrameCalculator](https://github.com/MikiraSora/ExtrameFunctionCalculator)������Ŀ����Ҫ������Ϊ����Ŀ�������Ľű����ԣ��ܸ��û���̬����/ж����Ԥ��д�õĺ����ļ�����˱���Ŀ��ʱ���ܷ�������ڸ���Ŀ��

---

##SEL�﷨
###Ԥ����
#### #package <�ű���>
�����������ڲ��ڽű����ݿ�ͷ������Ϊ�˽ű������֣���ĳ���ű���ű���ѯ���ú���ʱ������Դ�Ϊ���ݡ��û�Ҳ����ͨ��Calculator::GetScriptManager()::UnloadScript(<�ű���>)�ȷ�ʽ��ж�ش˽ű��ļ���
#### #version <������>
�������ѡ���ڽű����ݿ�ͷ������Ϊ�˽ű��İ汾�ţ���������������Ϊ��ʽ�汾�Ŷ�С��������Ϊ�޸Ļ���Ա�ǡ�
#### #include <����·��/���·��>
�������ѡ��������ڽű����ݿ�ͷ�����Ƚ���������ָ�����صĽű��Ƿ��Ѿ�����,���û�вŻ������Ķ�ȡ���أ�����еĻ�����������������"./xxx.cml"�������·�������Ե�ǰ�ļ��صĽű��ļ�·��Ŀ¼Ϊ��׼������Ҫ���صĽű��ļ���"g:\mymath\stdmath.cml"����ô#include ./stdmath_2.cml��ʵ��·������"g:\mymath\stdmath_2.cml"������·��������ͨ��������·������"g:\mymath\mat\stdmat.cml"��

###�����﷨
(ע��:�˽ű������޴����ţ�������xxx-endxxxΪ���)
####function-return-endfunction
    function getNewValue(a,b)
	    set a=a*10+b
	    return a
	endfunction
���������Ĳ������κ����Σ�ֻ�����֣�����ʱ���봫�ݵĲ���������һ�¡�
ÿ��function������һ��return�ұ����ǿ��Ա�ִ�е���,��Ϊfunction���趨��"��ʲô�ͱ�Ȼ����ʲô����

####set
    set a=a+1
�ұߵ�ֵ��Ȼ�����ɳ����ڸ�ֵ����ߵı���

####call <command\>

    call reg dymFunc(x)=x^5-1
call��GetCalculator.Execute(command),Ҳ����˵�ղ��Ƕδ��������Լ��󶨵ļ������ж�̬������һ������.

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
PS:���set a=a-1��Զִ�в���,just you know.

###ʾ��
####�ű�����
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

Ȼ������Ĵ������ôд:

    Calculator calculator=new Calculator();
    
    calculator.LoadScript("g:\stdmath2.cml");
    calculator.Solve("scriptFileTest()");
    /*���������ͬ��
    calculator.Execute("load_script g:\stdmath.cml");
    calcilator.Execute("solve scriptFileTest()");
    */
#����
###�Ѵ��ڵ�һЩ����
* ������ѯ����˳�����
* �������ù��̷���
* ż����ִ�й��̳��ִ���
* �����Ƿ�����

###�ƻ�ʵװ������
* ע��
* ��������
* ���ּ����Ż��ɳ�����ʾ
* �������븸��Ŀ
* �ѷ���ŷ
